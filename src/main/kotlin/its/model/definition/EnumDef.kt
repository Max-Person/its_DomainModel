package its.model.definition

import java.util.*

/**
 * Перечисление в домене ([Domain])
 */
class EnumDef(
    override val name: String,
) : DomainDefWithMeta() {

    override val description = "enum $name"
    override val reference = EnumRef(name)

    /**
     * Значения данного перечисления
     */
    val values = EnumValueContainer(this)
}

class EnumContainer(domain: Domain) : RootDefContainer<EnumDef>(domain)

class EnumRef(
    val enumName: String,
) : DomainRef {
    override fun findIn(domain: Domain) = domain.enums.get(enumName) as Optional<DomainDefWithMeta>
    override fun toString() = "enum $enumName"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EnumRef) return false

        if (enumName != other.enumName) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(this::class, enumName)
    }
}