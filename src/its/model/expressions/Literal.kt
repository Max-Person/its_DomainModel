package its.model.expressions

import its.model.expressions.literals.*
import its.model.expressions.types.ComparisonResult
import its.model.expressions.types.DataType
import java.lang.IllegalArgumentException

/**
 * Литерал в выражении
 * @param value Значение
 */
abstract class Literal(
    val value: String
) : Operator {

    override val args: List<Operator>
        get() = ArrayList()

    override val argsDataTypes: List<List<DataType>>
        get() = ArrayList()

    override fun clone(newArgs: List<Operator>): Operator {
        require(newArgs.isEmpty()) { "Для литерала аргументы не требуются." }
        return clone()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Literal

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + javaClass.hashCode()
        return result
    }


    companion object _static{
        @JvmStatic
        fun fromString(string: String) : Literal{
            //TODO
            when(string.lowercase()){
                "true" -> return BooleanLiteral(true)
                "false" -> return BooleanLiteral(false)
                else -> throw IllegalArgumentException("преобразование значения $string в литерал невозможно")
            }
        }
        
        @JvmStatic
        fun fromString(string: String, dataType: DataType, enumOwner: String? = null) : Literal{
            when(dataType){
                DataType.DecisionTreeVar -> return DecisionTreeVarLiteral(string)
                DataType.Class -> return ClassLiteral(string)
                DataType.Object -> return ObjectLiteral(string)
                DataType.Property -> return PropertyLiteral(string)
                DataType.Relationship -> return RelationshipLiteral(string)
                DataType.ComparisonResult -> return ComparisonResultLiteral(ComparisonResult.fromString(string)!!)
                DataType.String -> return StringLiteral(string)
                DataType.Boolean -> return BooleanLiteral(string.toBoolean())
                DataType.Integer -> return IntegerLiteral(string.toInt())
                DataType.Double -> return DoubleLiteral(string.toDouble())
                DataType.Enum -> return EnumLiteral(
                    string,
                    enumOwner!!,
                )
            }
        }
    }
}