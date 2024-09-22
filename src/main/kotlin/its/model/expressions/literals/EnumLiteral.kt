package its.model.expressions.literals

import its.model.definition.DomainModel
import its.model.definition.types.EnumType
import its.model.definition.types.EnumValue
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.LiteralBehaviour

/**
 * [EnumType] литерал
 */
class EnumLiteral(value: EnumValue) : ValueLiteral<EnumValue, EnumType>(value, EnumType(value.enumName)) {

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        if (!type.exists(domainModel)) {
            results.invalid(
                "No enum '${value.enumName}' found in domain, " +
                        "but it is used as an owner enum in $description"
            )
            return type
        }

        val enum = type.findIn(domainModel)
        results.checkConforming(
            enum.values.get(value.valueName) != null,
            "Enum '${value.enumName}' does not contain a '${value.valueName}' value, " +
                    "but it is used as one of its values in $description"
        )
        return type
    }

    override fun clone(): Operator = EnumLiteral(value)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}