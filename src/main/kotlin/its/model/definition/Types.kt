package its.model.definition

import its.model.models.AnyNumber
import its.model.models.Range
import java.util.*
import kotlin.reflect.KClass

sealed class Type<T : Any>(
    private val valueClass: KClass<T>,
) {
    open fun fits(value: Any, inDomain: Domain): Boolean {
        return fits(value)
    }

    protected open fun fits(value: Any): Boolean {
        return valueClass.isInstance(value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Type<*>

        return valueClass == other.valueClass
    }

    override fun hashCode(): Int {
        return Objects.hash(this::class, valueClass)
    }
}

object BooleanType : Type<Boolean>(Boolean::class)

sealed class NumericType<T : Number>(
    valueClass: KClass<T>,
    val range: Range,
) : Type<T>(valueClass) {

    override fun fits(value: Any): Boolean {
        return super.fits(value) && range.contains(value as Number)
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
                && range == (other as NumericType<*>).range
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), range)
    }
}

class IntegerType(
    range: Range = AnyNumber,
) : NumericType<Int>(Int::class, range)

class DoubleType(
    range: Range = AnyNumber,
) : NumericType<Double>(Double::class, range)

object StringType : Type<String>(String::class)


typealias EnumValue = EnumValueRef
class EnumType(
    val enumName: String,
) : Type<EnumValue>(EnumValue::class) {
    override fun fits(value: Any, inDomain: Domain): Boolean {
        if (!super.fits(value)) return false
        val enumValue = value as EnumValue

        val enumOpt = EnumRef(enumName).findIn(inDomain) as Optional<EnumDef>
        checkKnown(
            enumOpt.isPresent,
            "No enum definition '${enumName}' found to check if a value fits to a enum type"
        )

        val enum = enumOpt.get()
        return enum.name == enumValue.enumName && enum.values.get(enumValue.valueName).isPresent
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
                && enumName == (other as EnumType).enumName
    }
}

data class TypeAndValue<T : Any>(
    val type: Type<T>,
    val value: T,
)