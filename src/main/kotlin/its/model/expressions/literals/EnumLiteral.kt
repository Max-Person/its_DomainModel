package its.model.expressions.literals

import its.model.definition.Domain
import its.model.definition.types.EnumType
import its.model.definition.types.EnumValue
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.LiteralBehaviour
import kotlin.reflect.KClass

/**
 * [EnumType] литерал
 */
class EnumLiteral(value: EnumValue) : ValueLiteral<EnumValue, EnumType>(value, EnumType(value.enumName)) {

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        if (!type.exists(domain)) {
            results.invalid(
                "No enum '${value.enumName}' found in domain, " +
                        "but it is used as an owner enum in $description"
            )
            return type
        }

        val enum = type.findIn(domain)
        results.checkConforming(
            enum.values.get(value.valueName).isPresent,
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