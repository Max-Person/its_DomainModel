package its.model.definition

import java.util.*

/**
 * Модель объекта в домене ([Domain])
 */
class ObjectDef(
    override val name: String,
    val className: String,
) : DomainDefWithMeta(), MetaInheritor {

    override val inheritFrom
        get() = Optional.of(clazz as MetaInheritor)
    override val description = "object $name"
    override val reference = ObjectRef(name)

    /**
     * Значения свойств для данного объекта
     */
    internal val definedPropertyValues = ObjectPropertyValueStatements(this)

    /**
     * Связи данного объекта с другими
     */
    val relationshipLinks = RelationshipLinkStatements(this)

    /**
     * Для валидации - получить класс этого объекта,
     * или добавить сообщение о его неизвестности в [results]
     */
    internal fun getKnownClass(results: DomainValidationResults): Optional<ClassDef> {
        val clazzOpt = domain.classes.get(className)
        results.checkKnown(
            clazzOpt.isPresent,
            "No class definition '$className' found to be defined as $description's class"
        )
        return clazzOpt
    }

    /**
     * Для валидации - получить известную цепочку классов объекта
     * добавляя сообщение о неизвестных родителях в [results], если такие есть
     */
    internal fun getKnownInheritanceLineage(results: DomainValidationResults): List<ClassDef> {
        return getKnownClass(results).map { it.getKnownInheritanceLineage(results) }.orElse(emptyList())
    }

    /**
     * Валидация - найти определение свойства по имени; любые ошибки кладутся в [results]
     */
    internal fun findPropertyDef(propertyName: String, results: DomainValidationResults): Optional<PropertyDef> {
        val clazz = getKnownClass(results)
        if (clazz.isPresent) return clazz.get().findPropertyDef(propertyName, results)
        return Optional.empty()
    }

    /**
     * Валидация - найти определение отношения по имени; любые ошибки кладутся в [results]
     */
    internal fun findRelationshipDef(
        propertyName: String,
        results: DomainValidationResults
    ): Optional<RelationshipDef> {
        val clazz = getKnownClass(results)
        if (clazz.isPresent) return clazz.get().findRelationshipDef(propertyName, results)
        return Optional.empty()
    }

    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        getKnownClass(results)

        definedPropertyValues.validate(results)
        relationshipLinks.validate(results)
    }

    //---Операции (на валидном домене)---

    /**
     * Класс данного объекта
     */
    val clazz: ClassDef
        get() = getKnownClass(DomainValidationResultsThrowImmediately()).get()

    /**
     * Получить цепочку классов объекта
     */
    fun getInheritanceLineage() = getKnownInheritanceLineage(DomainValidationResultsThrowImmediately())

    /**
     * Является ли экземпляром класса
     */
    fun isInstance(className: String) = getInheritanceLineage().any { it.name == className }
    fun isInstance(classDef: ClassDef) = getInheritanceLineage().contains(classDef)

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

class ObjectContainer(domain: Domain) : RootDefContainer<ObjectDef>(domain)

class ObjectRef(
    val objectName: String,
) : DomainRef {
    override fun findIn(domain: Domain) = domain.objects.get(objectName) as Optional<DomainDefWithMeta>
    override fun toString() = "object $objectName"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjectRef) return false

        if (objectName != other.objectName) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(this::class, objectName)
    }
}