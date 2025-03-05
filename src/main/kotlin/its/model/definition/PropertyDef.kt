package its.model.definition

import its.model.definition.types.EnumType
import its.model.definition.types.Type
import java.util.*

/**
 * Определение свойства сущностей в домене
 */
class PropertyDef(
    val declaringClassName: String,
    override val name: String,
    val type: Type<*>,
    val kind: PropertyKind,
    val paramsDecl: ParamsDecl,
) : DomainDefWithMeta<PropertyDef>() {

    override val description = "${kind.toString().lowercase()} property $declaringClassName.$name"
    override val reference = PropertyRef(declaringClassName, name)

    enum class PropertyKind {
        CLASS,
        OBJECT,
        ;

        fun fits(valueOwner: ClassInheritorDef<*>): Boolean {
            return when (this) {
                CLASS -> valueOwner is ClassDef
                OBJECT -> valueOwner is ObjectDef
            }
        }
    }

    /**
     * Для валидации - получить класс, объявляющий свойство,
     * или добавить сообщение о его неизвестности в [results]
     */
    internal fun getKnownDeclaringClass(results: DomainValidationResults): ClassDef? {
        val clazz = domainModel.classes.get(declaringClassName)
        results.checkKnown(
            clazz != null,
            "No class definition '$declaringClassName' found, while $description is said to be declared in it"
        )
        return clazz
    }

    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        //Существование класса
        val owner = getKnownDeclaringClass(results)

        //Существование енама
        if (type is EnumType) {
            results.checkKnown(
                type.exists(domainModel),
                "No enum definition '${type.enumName}' found"
            )
        }

        //не перекрывает ли определения выше
        if (owner == null) return
        val lineage = owner.getKnownInheritanceLineage(results).minusElement(owner)
        for (parent in lineage) {
            if (parent.declaredProperties.get(name) != null) {
                results.invalid(
                    "property $name cannot be redeclared in ${owner.description}, " +
                            "as a property with the same name is already declared in superclass ${parent.description}."
                )
                break
            }
        }

        //Все параметры должны быть дискретными (с конечным множеством значений)
        for (param in paramsDecl) {
            results.checkValid(
                param.type.isDiscrete(),
                "All parameters in properties must have a discrete type," +
                        " but param '${param.name}' in ${owner.description} has a non-discrete type ${param.type}"
            )
        }

        //уникальность не проверяется т.к. PropertyContainer ее гарантирует
    }

    //----------------------------------

    override fun plainCopy() = PropertyDef(declaringClassName, name, type, kind, paramsDecl)

    override fun mergeEquals(other: PropertyDef): Boolean {
        if (!super.mergeEquals(other)) return false
        return declaringClassName == other.declaringClassName
                && name == other.name
                && type == other.type
                && kind == other.kind
    }


    //---Операции (на валидном домене)---

    /**
     * Класс, объявляющий свойство
     */
    val declaringClass: ClassDef
        get() = getKnownDeclaringClass(DomainValidationResultsThrowImmediately())!!
}

class PropertyContainer(clazz: ClassDef) : ChildDefContainer<PropertyDef, ClassDef>(clazz)

class PropertyRef(
    val className: String,
    val propertyName: String,
) : DomainRef<PropertyDef> {
    override fun findIn(domainModel: DomainModel): PropertyDef? {
        val clazz = ClassRef(className).findIn(domainModel) ?: return null
        return clazz.declaredProperties.get(propertyName)
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