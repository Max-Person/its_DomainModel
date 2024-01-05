package its.model.definition

import its.model.definition.types.Comparison
import its.model.definition.types.EnumValue
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

    override val isEmpty: Boolean
        get() = super.isEmpty
                && values.isEmpty()

    override fun subtract(other: EnumDef) {
        if (!this.mergeEquals(other)) return
        values.subtract(other.values)
    }

}

class EnumContainer(domain: Domain) : RootDefContainer<EnumDef>(domain) {
    init {
        fun EnumValue.toDef() = EnumValueDef(enumName, valueName)

        addBuiltIn(EnumDef(Comparison.Type.enumName)).also {
            it.values.add(Comparison.Values.Less.toDef())
            it.values.add(Comparison.Values.Greater.toDef())
            it.values.add(Comparison.Values.Equal.toDef())
        }
    }
}

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