package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.AnyType
import its.model.definition.types.BooleanType
import its.model.definition.types.NumericType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Сравнение с явным указанием оператора
 *
 * Возвращает [BooleanType]
 * @param firstExpr первое сравниваемое значение ([AnyType] для (не)равенств, иначе [NumericType])
 * @param operator оператор сравнения
 * @param secondExpr второе сравниваемое значение ([AnyType] для (не)равенств, иначе [NumericType])
 */
class CompareWithComparisonOperator(
    val firstExpr: Operator,
    val operator: ComparisonOperator,
    val secondExpr: Operator,
) : Operator() {

    enum class ComparisonOperator {

        /**
         * Меньше
         */
        Less,

        /**
         * Больше
         */
        Greater,

        /**
         * Равно
         */
        Equal,

        /**
         * Меньше или равно
         */
        LessEqual,

        /**
         * Больше или равно
         */
        GreaterEqual,

        /**
         * Не равно
         */
        NotEqual,

        ;

        val isEquality
            get() = this == Equal || this == NotEqual


        companion object {

            @JvmStatic
            fun fromString(value: String) = when (value.uppercase()) {
                "LESS", "LT" -> Less
                "GREATER", "GT" -> Greater
                "EQ", "EQUAL" -> Equal
                "LESSEQ", "LE", "LESSEQUAL", "LESS_EQ", "LESS_EQUAL" -> LessEqual
                "GREATEREQ", "GE", "GREATEREQUAL", "GREATER_EQ", "GREATER_EQUAL" -> GreaterEqual
                "NOTEQ", "NE", "NOTEQUAL", "NOT_EQ", "NOT_EQUAL" -> NotEqual
                else -> throw IllegalArgumentException("Cannot convert String '$value' into a ComparisonOperator")
            }
        }
    }

    override val children: List<Operator>
        get() = listOf(firstExpr, secondExpr)

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val firstType = firstExpr.validateAndGetType(domainModel, results, context)
        val secondType = secondExpr.validateAndGetType(domainModel, results, context)
        results.checkValid(
            operator.isEquality || (firstType is NumericType && secondType is NumericType),
            "Comparison operator $operator is not compatible with non-numeric types " +
                    "(trying to compare values of types '$firstType' and '$secondType')"
        )
        return BooleanType
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}