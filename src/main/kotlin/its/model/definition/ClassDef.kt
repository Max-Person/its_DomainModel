package its.model.definition

import java.util.*

/**
 * Класс в домене ([Domain])
 */
class ClassDef(
    override val name: String,
    var parentName: Optional<String> = Optional.empty(),
) : DomainDefWithMeta(), MetaInheritor {

    override val inheritFrom
        get() = parent as Optional<MetaInheritor>
    override val description = "class $name"
    override val reference = ClassRef(name)

    /**
     * Определяемые данным классом свойства
     */
    val declaredProperties = PropertyContainer(this)

    /**
     * Определяемые данным классом отношения
     */
    val declaredRelationships = RelationshipContainer(this)

    /**
     * Значения для свойств, указанные в данном классе
     */
    internal val definedPropertyValues = ClassPropertyValueStatements(this)

    /**
     * Для валидации - получить родителя класса,
     * или добавить сообщение о его неизвестности в [results]
     */
    internal fun getKnownParent(results: DomainValidationResults): Optional<ClassDef> {
        if (!parentName.isPresent) return Optional.empty()

        val clazzOpt = domain.classes.get(parentName.get())
        results.checkKnown(
            clazzOpt.isPresent,
            "No class definition '${parentName.get()}' found to be defined as $name's parent class"
        )
        return clazzOpt
    }

    /**
     * Для валидации - получить известную цепочку родителей класса, включая его самого,
     * добавляя сообщение о неизвестных родителях в [results], если такие есть
     */
    internal fun getKnownInheritanceLineage(results: DomainValidationResults): List<ClassDef> {
        val lineage = mutableListOf<ClassDef>()
        var p = Optional.of(this)
        while (p.isPresent) {
            lineage.add(p.get())
            p = p.get().getKnownParent(results)
            if (p.isPresent && p.get() === this) {
                results.invalid("$description is a supertype of itself (lineage is ${lineage.map { it.name }})")
                break
            }
        }
        return lineage
    }

    /**
     * Валидация - найти определение свойства по имени; любые ошибки кладутся в [results]
     */
    internal fun findPropertyDef(propertyName: String, results: DomainValidationResults): Optional<PropertyDef> {
        for (clazz in getKnownInheritanceLineage(results)) {
            val found = clazz.declaredProperties.get(propertyName)
            if (found.isPresent) return found
        }
        return Optional.empty()
    }

    /**
     * Валидация - найти определение отношения по имени; любые ошибки кладутся в [results]
     */
    internal fun findRelationshipDef(
        relationshipName: String,
        results: DomainValidationResults
    ): Optional<RelationshipDef> {
        for (clazz in getKnownInheritanceLineage(results)) {
            val found = clazz.declaredRelationships.get(relationshipName)
            if (found.isPresent) return found
        }
        return Optional.empty()
    }

    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        //Существование родителя и ацикличность зависимостей
        val parentOpt = getKnownInheritanceLineage(results)

        declaredProperties.validate(results)
        declaredRelationships.validate(results)
        definedPropertyValues.validate(results)
    }

    //---Операции (на валидном домене)---

    /**
     * Родитель этого класса
     */
    val parent: Optional<ClassDef>
        get() = getKnownParent(DomainValidationResultsThrowImmediately())

    /**
     * Получить цепочку родителей класса, **включая его самого**
     */
    fun getInheritanceLineage() = getKnownInheritanceLineage(DomainValidationResultsThrowImmediately())

    /**
     * Прямые классы-наследники данного класса
     */
    val children: List<ClassDef>
        get() = domain.classes.filter { clazz -> clazz.parentName.map { it == this.name }.orElse(false) }

    /**
     * Прямые экземпляры данного класса
     */
    val directInstances: List<ObjectDef>
        get() = domain.objects.filter { obj -> obj.className == this.name }

    /**
     * Является ли класс конкретным (финальным в некоторой цепочке наследования)
     */
    val isConcrete: Boolean
        get() = domain.classes.none { clazz -> clazz.parentName.map { it == this.name }.orElse(false) } //Нет детей
                || domain.objects.any { obj -> obj.className == this.name } //есть экземпляры


    /**
     * Получить значение свойства с учетом наследования
     * @throws DomainNonConformityException если такого свойства не существует
     */
    fun getPropertyValue(propertyName: String): Any {
        checkConforming(
            findPropertyDef(propertyName, DomainValidationResultsThrowImmediately()).isPresent,
            "No property $propertyName exists for $description"
        )
        for (clazz in getInheritanceLineage()) {
            val found = definedPropertyValues.get(propertyName)
            if (found.isPresent) return found.get()
        }
        throw ThisShouldNotHappen()
    }

}

class ClassContainer(domain: Domain) : RootDefContainer<ClassDef>(domain) {
    override fun add(def: ClassDef): ClassDef {
        return super.add(def).also { domain.separateClassPropertyValues.claimIfPresent(it) }
    }
}

class ClassRef(
    val className: String,
) : DomainRef {
    override fun findIn(domain: Domain) = domain.classes.get(className) as Optional<DomainDefWithMeta>
    override fun toString() = "class $className"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClassRef) return false

        if (className != other.className) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(this::class, className)
    }
}