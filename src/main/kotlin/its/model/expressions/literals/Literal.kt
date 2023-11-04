package its.model.expressions.literals

import its.model.expressions.Operator
import its.model.expressions.types.ComparisonResult
import its.model.expressions.types.EnumValue
import its.model.expressions.types.Types
import its.model.expressions.visitors.LiteralBehaviour
import its.model.expressions.visitors.OperatorBehaviour
import kotlin.reflect.KClass

/**
 * Литерал в выражении
 */
sealed interface Literal : Operator {

    override val args: List<Operator>
        get() = ArrayList()

    override val argsDataTypes: List<List<KClass<*>>>
        get() = ArrayList()

    override fun clone(newArgs: List<Operator>): Operator {
        require(newArgs.isEmpty()) { "Для литерала аргументы не требуются." }
        return clone()
    }

    fun <I> use(behaviour: LiteralBehaviour<I>): I
    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return use(behaviour as LiteralBehaviour<I>)
    }

    companion object _static {
        @JvmStatic
        fun fromString(string: String): Literal {
            //TODO
            when (string.lowercase()) {
                "true" -> return BooleanLiteral(true)
                "false" -> return BooleanLiteral(false)
                else -> throw IllegalArgumentException("преобразование значения $string в литерал невозможно")
            }
        }

        @JvmStatic
        fun fromString(string: String, dataType: KClass<*>, enumOwner: String? = null): Literal {
            when (dataType) {
                Types.Class -> return ClassRef(string)
                Types.Object -> return ObjectRef(string)
                Types.String -> return StringLiteral(string)
                Types.Boolean -> return BooleanLiteral(string.toBoolean())
                Types.Integer -> return IntegerLiteral(string.toInt())
                Types.Double -> return DoubleLiteral(string.toDouble())
                Types.ComparisonResult -> return ComparisonResultLiteral(ComparisonResult.fromString(string)!!)
                Types.Enum -> return EnumLiteral(
                    EnumValue(
                        enumOwner!!,
                        string,
                    )
                )

                else -> throw IllegalArgumentException("Некорректный тип данных $dataType")
            }
        }
    }
}