package its.model.definition

import its.model.models.AnyNumber
import its.model.models.Range
import java.util.*
import kotlin.reflect.KClass

sealed class Type<T : Any>(
    private val valueClass: KClass<T>,
) {
    open fun fits(value: Any): Boolean {
        return valueClass.isInstance(value)
    }
}

class BooleanType : Type<Boolean>(Boolean::class)

sealed class NumericType<T : Number>(
    valueClass: KClass<T>,
    val range: Range,
) : Type<T>(valueClass) {

    override fun fits(value: Any): Boolean {
        return super.fits(value) && range.contains(value as Number)
    }
}

class IntegerType(
    range: Range = AnyNumber,
) : NumericType<Int>(Int::class, range)

class DoubleType(
    range: Range = AnyNumber,
) : NumericType<Double>(Double::class, range)

class StringType : Type<String>(String::class)

class EnumType(
    val domain: Domain,
    val enumName: String,
) : Type<EnumValueRef>(EnumValueRef::class) {
    override fun fits(value: Any): Boolean {
        if (!super.fits(value)) return false
        val enumValue = value as EnumValueRef

        val enumOpt = EnumRef(enumName).findIn(domain) as Optional<EnumDef>
        checkKnown(
            enumOpt.isPresent,
            "No enum definition '${enumName}' found to check if a value fits to a enum type"
        )

        val enum = enumOpt.get()
        return enum.name == enumValue.enumName && enum.values.get(enumValue.valueName).isPresent
    }
}

data class TypeAndValue<T : Any>(
    val type: Type<T>,
    val value: T,
)