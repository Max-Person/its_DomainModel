package its.model.expressions.utils

import its.model.definition.DomainModel
import its.model.definition.ParamsDecl
import its.model.definition.ParamsValues
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator

/**
 * Вспомогательный класс для указания списка параметров в выражениях. См. [ParamsDecl] и [ParamsValues]
 */
sealed class ParamsValuesExprList {
    abstract fun asMap(paramsDecl: ParamsDecl): Map<String, Operator>

    abstract fun getExprList(): List<Operator>

    internal open fun validateFull(
        paramsDecl: ParamsDecl,
        mainOperator: Operator,
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext,
    ) {
        validatePartial(paramsDecl, mainOperator, domainModel, results, context)
        //слишком мало параметров
        val values = this.asMap(paramsDecl)
        results.checkConforming(
            values.size >= paramsDecl.size,
            "Too few params are given for ${mainOperator.description}, expected ${paramsDecl.size}, but ${values.size} are present"
        )
    }

    internal open fun validatePartial(
        paramsDecl: ParamsDecl,
        mainOperator: Operator,
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext,
    ) {
        //слишком много параметров
        val values = this.asMap(paramsDecl)
        results.checkConforming(
            values.size <= paramsDecl.size,
            "Too many params are given for ${mainOperator.description}, expected ${paramsDecl.size}, but ${values.size} are present"
        )
    }

    companion object {
        @JvmStatic
        val EMPTY = NamedParamsValuesExprList(mapOf())
    }
}

/**
 * Упорядоченный набор значений параметров.
 * Использует порядок, в котором указаны значения, для определения того, какому из параметров они соответствуют
 * - так можно не указывать названия самих параметров.
 */
class OrderedParamsValuesExprList(
    val values: List<Operator>,
) : ParamsValuesExprList() {
    override fun asMap(paramsDecl: ParamsDecl): Map<String, Operator> {
        return values.mapIndexed { index, value ->
            paramsDecl[index].name to value
        }.toMap()
    }

    override fun getExprList(): List<Operator> {
        return values
    }

    override fun validatePartial(
        paramsDecl: ParamsDecl,
        mainOperator: Operator,
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext,
    ) {
        super.validatePartial(paramsDecl, mainOperator, domainModel, results, context)
        values.forEachIndexed { index, valueExpr ->
            val param = paramsDecl[index]
            val valueType = valueExpr.validateAndGetType(domainModel, results, context)
            results.checkConforming(
                param.type.castFits(valueType, domainModel),
                "Value at pos $index in parameters of $mainOperator has type $valueType" +
                        " and does not match the expected type of parameter '${param.name}' (${param.type})"
            )
        }
    }
}

/**
 * Именованный набор значений параметров.
 * Использует явно заданные названия параметров для определения того, какому из параметров соответствует какое значение
 * - это понятнее читать и позволяет указывать значения в произвольном порядке.
 */
class NamedParamsValuesExprList(
    val valuesMap: Map<String, Operator>,
) : ParamsValuesExprList() {
    override fun asMap(paramsDecl: ParamsDecl): Map<String, Operator> {
        return valuesMap
    }

    override fun getExprList(): List<Operator> {
        return valuesMap.values.toList()
    }

    override fun validatePartial(
        paramsDecl: ParamsDecl,
        mainOperator: Operator,
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext,
    ) {
        super.validatePartial(paramsDecl, mainOperator, domainModel, results, context)
        valuesMap.forEach { (paramName, valueExpr) ->
            val param = paramsDecl[paramName]
            val valueType = valueExpr.validateAndGetType(domainModel, results, context)
            if (param != null) {
                results.checkConforming(
                    param.type.castFits(valueType, domainModel),
                    "Value for parameter '${param.name}' has type $valueType" +
                            " and does not match its expected type (${param.type}) in $mainOperator"
                )
            } else {
                results.nonConforming(
                    "No parameter with name '$paramName' exists to use in $mainOperator"
                )
            }
        }
    }
}