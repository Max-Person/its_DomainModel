package its.model.definition.types

import its.model.definition.*
import java.util.*
import kotlin.reflect.KClass

/**
 * Тип значения при вычислении
 */
sealed class Type<T : Any>(
    private val valueClass: KClass<T>,
) {

    /**
     * Можно ли привести значение [value] к данному типу
     */
    open fun fits(value: Any, inDomainModel: DomainModel): Boolean {
        return fits(value)
    }

    /**
     * @see fits
     */
    protected open fun fits(value: Any): Boolean {
        return valueClass.isInstance(value)
    }

    /**
     * Можно ли привести тип [subType] к данному типу
     */
    open fun castFits(subType: Type<*>, inDomainModel: DomainModel): Boolean {
        return castFits(subType)
    }

    /**
     * @see castFits
     */
    protected open fun castFits(subType: Type<*>): Boolean {
        return this == subType
    }

    open fun isDiscrete(): Boolean = false

    open fun getDiscreteValues(inDomainModel: DomainModel): Set<T>? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Type<*>

        return valueClass == other.valueClass
    }

    override fun hashCode(): Int {
        return Objects.hash(this::class, valueClass)
    }

    companion object {
        @JvmStatic
        fun of(value: Any): Type<*> {
            return when (value) {
                is Boolean -> BooleanType
                is Int -> IntegerType(value)
                is Double -> DoubleType(value)
                is String -> StringType
                is EnumValue -> EnumType(value.enumName)
                is Clazz -> ClassType(value.className)
                is Obj -> ObjectType.untyped() //FIXME?
                else -> AnyType
            }
        }
    }

    override fun toString() = this::class.simpleName.toString()
}

/**
 * Любое значение
 */
object AnyType : Type<Any>(Any::class) {
    override fun castFits(subType: Type<*>): Boolean {
        return subType !is NoneType //К Any можно привести любой тип, кроме несуществующего
    }
}

/**
 * Несуществующий тип (отсутствие значения)
 */
object NoneType : Type<Nothing>(Nothing::class)

/**
 * Логический (булев) тип
 */
object BooleanType : Type<Boolean>(Boolean::class) {
    override fun isDiscrete(): Boolean = true
    override fun getDiscreteValues(inDomainModel: DomainModel): Set<Boolean> = setOf(true, false)
}

/**
 * Численный тип
 * @param range диапазон значений, допускаемых в данном типе
 */
sealed class NumericType<T : Number>(
    valueClass: KClass<T>,
    val range: Range,
) : Type<T>(valueClass) {

    override fun fits(value: Any): Boolean {
        return super.fits(value) && range.contains(value as Number)
    }

    override fun castFits(subType: Type<*>): Boolean {
        if (subType !is NumericType) return false
        if (this is IntegerType && subType is DoubleType) return false

        return this.range.contains(subType.range)
    }

    override fun isDiscrete(): Boolean {
        return range is DiscreteRange
    }

    override fun getDiscreteValues(inDomainModel: DomainModel): Set<T>? {
        if (range !is DiscreteRange) return null
        return range.values.map { it.asValueType() }.filterNotNull().toSet()
    }

    protected abstract fun Double.asValueType(): T?

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
                && range == (other as NumericType<*>).range
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), range)
    }

    override fun toString(): String {
        return super.toString() + range.modString
    }
}

/**
 * Целочисленный тип
 */
class IntegerType(
    range: Range = AnyNumber,
) : NumericType<Int>(Int::class, range) {
    constructor(value: Int) : this(DiscreteRange(setOf(value.toDouble())))

    override fun Double.asValueType(): Int? {
        val int = this.toInt()
        return if (this != int.toDouble()) null else int
    }
}

/**
 * Вещественный тип
 */
class DoubleType(
    range: Range = AnyNumber,
) : NumericType<Double>(Double::class, range) {
    constructor(value: Double) : this(DiscreteRange(setOf(value)))

    override fun Double.asValueType(): Double {
        return this
    }
}

/**
 * Строковый тип
 */
object StringType : Type<String>(String::class)

/**
 * Ссылочный тип, ссылающийся на определение в домене
 */
sealed class DomainRefType<Value : Any, Ref : DomainRef<Def>, Def : DomainDef<Def>>(
    valueClass: KClass<Value>,
) : Type<Value>(valueClass) {
    /**
     * Ссылка, соответствующая данному типу
     */
    abstract val reference: Ref

    /**
     * Существует ли тип в домене
     */
    open fun exists(domainModel: DomainModel): Boolean = reference.findIn(domainModel) != null

    /**
     * Найти соответствующее типу определение в домене
     */
    fun findIn(domainModel: DomainModel): Def = reference.findInOrUnkown(domainModel)

    override fun fits(value: Any, inDomainModel: DomainModel): Boolean {
        if (!this.exists(inDomainModel)) return false
        return super.fits(value, inDomainModel)
    }

    override fun castFits(subType: Type<*>, inDomainModel: DomainModel): Boolean {
        if (!this.exists(inDomainModel)) return false
        return super.castFits(subType)
    }
}

typealias EnumValue = EnumValueRef

/**
 * Перечисляемый тип
 * @param enumName название перечисления, значения которого которого допустимы данным типом
 */
open class EnumType(
    val enumName: String,
) : DomainRefType<EnumValue, EnumRef, EnumDef>(EnumValue::class) {

    override val reference: EnumRef
        get() = EnumRef(enumName)

    override fun fits(value: Any, inDomainModel: DomainModel): Boolean {
        if (!super.fits(value, inDomainModel)) return false

        value as EnumValue

        val enum = this.findIn(inDomainModel)
        return enum.name == value.enumName && enum.values.get(value.valueName) != null
    }

    override fun isDiscrete(): Boolean = true

    override fun getDiscreteValues(inDomainModel: DomainModel): Set<EnumValue>? {
        if (!this.exists(inDomainModel)) return null

        return this.findIn(inDomainModel).values.map { it.reference }.toSet()
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
                && enumName == (other as EnumType).enumName
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), enumName)
    }

    override fun toString(): String {
        return super.toString() + "($enumName)"
    }
}

/**
 * Тип "Результат сравнения"
 */
object Comparison {
    @JvmField
    val Type = EnumType("Comparison")

    object Values {
        @JvmField
        val Less = EnumValue(Type.enumName, "less")

        @JvmField
        val Greater = EnumValue(Type.enumName, "greater")

        @JvmField
        val Equal = EnumValue(Type.enumName, "equal")
    }
}


/**
 * Тип наследников класса
 * @param className название класса, наследники которого допустимы данным типом
 */
sealed class ClassInheritorType<Value : DomainRef<Inheritor>, Inheritor : ClassInheritorDef<Inheritor>>(
    val className: String,
    valueClass: KClass<Value>,
) : DomainRefType<Value, ClassRef, ClassDef>(valueClass) {

    protected val isUntyped
        get() = className == UNTYPED

    override fun exists(domainModel: DomainModel): Boolean = !isUntyped && super.exists(domainModel)

    override val reference: ClassRef
        get() = ClassRef(className)

    override fun fits(value: Any, inDomainModel: DomainModel): Boolean {
        if (!super.fits(value, inDomainModel)) return false
        value as Value

        return value.findIn(inDomainModel)?.inheritsFrom(this.findIn(inDomainModel)) ?: false
    }

    override fun castFits(subType: Type<*>, inDomainModel: DomainModel): Boolean {
        if (subType::class != this::class) return false
        subType as ClassInheritorType<Value, Inheritor>
        if (!this.exists(inDomainModel) || !subType.exists(inDomainModel)) return false

        val thisClazz = this.findIn(inDomainModel)
        val otherClazz = subType.findIn(inDomainModel)
        return otherClazz.isSubclassOf(thisClazz)
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
                && className == (other as ClassInheritorType<Value, Inheritor>).className
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), className)
    }

    override fun toString(): String {
        return super.toString() + "($className)"
    }

    companion object {
        /**
         * @see [DomainDef.isValidName]
         */
        @JvmStatic
        protected val UNTYPED = "unknown class"
    }
}

typealias Clazz = ClassRef

/**
 * Тип Класс
 */
class ClassType(className: String) : ClassInheritorType<Clazz, ClassDef>(className, Clazz::class) {
    fun toObjectType() = ObjectType(className)

    companion object {
        @JvmStatic
        fun untyped() = ClassType(UNTYPED)
    }
}

typealias Obj = ObjectRef

/**
 * Тип Объект
 */
class ObjectType(className: String) : ClassInheritorType<Obj, ObjectDef>(className, Obj::class) {
    fun toClassType() = ClassType(className)

    companion object {
        @JvmStatic
        fun untyped() = ObjectType(UNTYPED)
    }

    fun projectFits(subType: Type<*>, inDomainModel: DomainModel): Boolean {
        if (subType::class != this::class) return false
        subType as ObjectType
        if (!this.exists(inDomainModel) || !subType.exists(inDomainModel)) return false

        val thisClazz = this.findIn(inDomainModel)
        val otherClazz = subType.findIn(inDomainModel)
        return otherClazz.canBeProjectedOnto(thisClazz)
    }
}

data class TypeAndValue<T : Any>(
    val type: Type<T>,
    val value: T,
)