package its.model.definition

import java.util.*

/**
 * Одно из значений перечисления [EnumDef]
 */
class EnumValueDef(
    val enumName: String,
    override val name: String,
) : DomainDefWithMeta() {

    override val description = "enum value $enumName:$name"
    override val reference = EnumValueRef(enumName, name)

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
) : DomainRef {
    override fun findIn(domain: Domain): Optional<DomainDefWithMeta> {
        val enumOpt = EnumRef(enumName).findIn(domain) as Optional<EnumDef>
        if (!enumOpt.isPresent) return Optional.empty()
        return enumOpt.get().values.get(valueName) as Optional<DomainDefWithMeta>
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