package its.model.expressions.literals

abstract class ValueLiteral<Type : Any>(
    val value: Type,
) : Literal {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ValueLiteral<*>

        if (resultDataType != other.resultDataType) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + javaClass.hashCode()
        result = 31 * result + resultDataType.hashCode()
        return result
    }
}