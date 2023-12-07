package its.model.definition

import java.util.*

/**
 * Перечисление в домене ([Domain])
 */
class EnumDef(
    override val name: String,
) : DomainDefWithMeta<EnumDef>() {

    override val description = "enum $name"
    override val reference = EnumRef(name)

    /**
     * Значения данного перечисления
     */
    val values = EnumValueContainer(this)

    //----------------------------------

    override fun plainCopy() = EnumDef(name)

    override fun mergeEquals(other: EnumDef): Boolean {
        if (!super.mergeEquals(other)) return false
        return name == other.name
    }

    override fun addMerge(other: EnumDef) {
        super.addMerge(other)
        values.addAllMerge(other.values)
    }

}

class EnumContainer(domain: Domain) : RootDefContainer<EnumDef>(domain)

class EnumRef(
    val enumName: String,
) : DomainRef<EnumDef> {
    override fun findIn(domain: Domain) = domain.enums.get(enumName)
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