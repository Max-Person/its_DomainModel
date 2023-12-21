package its.model.expressions.literals

import its.model.definition.Domain
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import java.util.Objects

/**
 * Литерал, содержащий конкретное значение
 * @param value значение данного литерала
 * @param type тип значения данного литерала
 */
abstract class ValueLiteral<V : Any, T : Type<V>>(
    val value: V,
    val type: T,
) : Literal() {

    override val description: String
        get() = "${super.description} '$value'"

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        return type
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ValueLiteral<V, T>

        if (type != other.type) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(value, type)
    }
}