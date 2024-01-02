package its.model.definition

import java.util.*

/**
 * Одно из значений перечисления [EnumDef]
 */
class EnumValueDef(
    val enumName: String,
    override val name: String,
) : DomainDefWithMeta<EnumValueDef>() {

    override val description = "enum value $enumName:$name"
    override val reference = EnumValueRef(enumName, name)

    //----------------------------------

    override fun plainCopy() = EnumValueDef(enumName, name)

    override fun mergeEquals(other: EnumValueDef): Boolean {
        if (!super.mergeEquals(other)) return false
        return enumName == other.enumName
                && name == other.name
    }

}

class EnumValueContainer(enum: EnumDef) : ChildDefContainer<EnumValueDef, EnumDef>(enum)

/**
 * Ссылка на значение перечисления
 *
 * Этот же класс используется при оперировании значения перечислений (например, в выражениях)
 * @see EnumType
 */
class EnumValueRef(
    val enumName: String,
    val valueName: String,
) : DomainRef<EnumValueDef> {
    override fun findIn(domain: Domain): EnumValueDef? {
        val enum = EnumRef(enumName).findIn(domain) ?: return null
        return enum.values.get(valueName)
    }

    override fun toString() = "$enumName:$valueName"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EnumValueRef) return false

        if (enumName != other.enumName) return false
        if (valueName != other.valueName) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(this::class, enumName, valueName)
    }
}