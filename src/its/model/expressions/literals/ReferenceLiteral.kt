package its.model.expressions.literals

abstract class ReferenceLiteral(
    val name : String,
) : Literal {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ValueLiteral<*>

        if (resultDataType != other.resultDataType) return false
        if (name != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + javaClass.hashCode()
        result = 31 * result + resultDataType.hashCode()
        return result
    }
}