package its.model.expressions

import its.model.expressions.literals.BooleanLiteral
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
    }
}