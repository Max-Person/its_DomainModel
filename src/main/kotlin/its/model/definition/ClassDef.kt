package its.model.definition

import java.util.*

/**
 * Класс в домене ([Domain])
 */
class ClassDef(
    override val name: String,
    val parentName: Optional<String> = Optional.empty(),
) : ClassInheritorDef<ClassDef>() {

    override val parentClassName: Optional<String>
        get() = parentName

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
    override val definedPropertyValues = ClassPropertyValueStatements(this)


    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        //Существование родителя и ацикличность зависимостей
        val parentOpt = getKnownInheritanceLineage(results)

        declaredProperties.validate(results)
        declaredRelationships.validate(results)
        definedPropertyValues.validate(results)
    }

    //----------------------------------

    override fun plainCopy() = ClassDef(name, parentName)

    override fun mergeEquals(other: ClassDef): Boolean {
        if (!super.mergeEquals(other)) return false
        return name == other.name
                && parentName == other.parentName
    }

    override fun addMerge(other: ClassDef) {
        super.addMerge(other)
        declaredProperties.addAllMerge(other.declaredProperties)
        declaredRelationships.addAllMerge(other.declaredRelationships)

        definedPropertyValues.addAll(other.definedPropertyValues)
    }

    //---Операции (на валидном домене)---

    /**
     * Является ли подтипом класса
     *
     * (alias для [inheritsFrom])
     */
    fun isSubclassOf(className: String) = inheritsFrom(className)

    /**
     * @see isSubclassOf
     */
    fun isSubclassOf(classDef: ClassDef) = inheritsFrom(classDef)

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

}

class ClassContainer(domain: Domain) : RootDefContainer<ClassDef>(domain) {
    override fun addNew(def: ClassDef): ClassDef {
        return super.addNew(def).also { domain.separateClassPropertyValues.claimIfPresent(it) }
    }
}

class ClassRef(
    val className: String,
) : DomainRef<ClassDef> {
    override fun findIn(domain: Domain) = domain.classes.get(className)
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