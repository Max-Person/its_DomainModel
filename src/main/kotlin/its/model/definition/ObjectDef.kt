package its.model.definition

import java.util.*

/**
 * Модель объекта в домене ([Domain])
 */
class ObjectDef(
    override val name: String,
    val className: String,
) : ClassInheritorDef<ObjectDef>() {

    override val parentClassName: Optional<String>
        get() = Optional.of(className)

    override val description = "object $name"
    override val reference = ObjectRef(name)

    /**
     * Значения свойств для данного объекта
     */
    override val definedPropertyValues = ObjectPropertyValueStatements(this)

    /**
     * Связи данного объекта с другими
     */
    val relationshipLinks = RelationshipLinkStatements(this)

    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        getKnownParentClass(results)

        definedPropertyValues.validate(results)
        relationshipLinks.validate(results)
    }

    //----------------------------------

    override fun plainCopy() = ObjectDef(name, className)

    override fun mergeEquals(other: ObjectDef): Boolean {
        if (!super.mergeEquals(other)) return false
        return name == other.name
                && className == other.className
    }

    override fun addMerge(other: ObjectDef) {
        super.addMerge(other)
        definedPropertyValues.addAll(other.definedPropertyValues)
        relationshipLinks.addAll(other.relationshipLinks)
    }

    //---Операции (на валидном домене)---

    /**
     * Класс данного объекта
     */
    val clazz: ClassDef
        get() = parentClass.get()

    /**
     * Является ли экземпляром класса
     *
     * (alias для [inheritsFrom])
     */
    fun isInstanceOf(className: String) = inheritsFrom(className)
    /**
     * @see isInstanceOf
     */
    fun isInstanceOf(classDef: ClassDef) = inheritsFrom(classDef)
}

class ObjectContainer(domain: Domain) : RootDefContainer<ObjectDef>(domain)

class ObjectRef(
    val objectName: String,
) : DomainRef<ObjectDef> {
    override fun findIn(domain: Domain) = domain.objects.get(objectName)
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