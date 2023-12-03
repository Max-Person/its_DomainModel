package its.model.definition

import java.util.*

/**
 * Определение свойства сущностей в домене
 */
class PropertyDef(
    var declaringClassName: String,
    override val name: String,
    var type: Type<*>,
    var kind: PropertyKind,
) : DomainDefWithMeta() {

    override val description = "${kind.toString().lowercase()} property $declaringClassName.$name"
    override val reference = PropertyRef(declaringClassName, name)

    enum class PropertyKind {
        CLASS,
        OBJECT,
    }

    /**
     * Для валидации - получить класс, объявляющий свойство,
     * или добавить сообщение о его неизвестности в [results]
     */
    internal fun getKnownDeclaringClass(results: DomainValidationResults): Optional<ClassDef> {
        val clazzOpt = domain.classes.get(declaringClassName)
        results.checkKnown(
            clazzOpt.isPresent,
            "No class definition '$declaringClassName' found, while $description is said to be declared in it"
        )
        return clazzOpt
    }

    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        //Существование класса
        val ownerOpt = getKnownDeclaringClass(results)

        //Существование енама
        if (type is EnumType) {
            results.checkKnown(
                domain.enums.get((type as EnumType).enumName).isPresent,
                "No enum definition '${(type as EnumType).enumName}' found"
            )
        }

        //не перекрывает ли определения выше
        if (!ownerOpt.isPresent) return
        val owner = ownerOpt.get()
        val lineage = owner.getKnownInheritanceLineage(results).minusElement(owner)
        for (parent in lineage) {
            if (parent.declaredProperties.get(name).isPresent) {
                results.invalid(
                    "property $name cannot be redeclared in ${owner.description}, " +
                            "as a property with the same name is already declared in superclass ${parent.description}."
                )
                break
            }
        }

        //уникальность не проверяется т.к. PropertyContainer ее гарантирует
    }

    //---Операции (на валидном домене)---

    /**
     * Класс, объявляющий свойство
     */
    val declaringClass: ClassDef
        get() = getKnownDeclaringClass(DomainValidationResultsThrowImmediately()).get()
}

class PropertyContainer(clazz: ClassDef) : ChildDefContainer<PropertyDef, ClassDef>(clazz)

class PropertyRef(
    val className: String,
    val propertyName: String,
) : DomainRef {
    override fun findIn(domain: Domain): Optional<DomainDefWithMeta> {
        val classOpt = ClassRef(className).findIn(domain) as Optional<ClassDef>
        if (!classOpt.isPresent) return Optional.empty()
        return classOpt.get().declaredProperties.get(propertyName) as Optional<DomainDefWithMeta>
    }

    override fun toString() = "$className.$propertyName"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PropertyRef) return false

        if (className != other.className) return false
        if (propertyName != other.propertyName) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(this::class, className, propertyName)
    }
}