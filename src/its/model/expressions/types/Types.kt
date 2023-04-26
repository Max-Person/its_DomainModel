package its.model.expressions.types

import its.model.expressions.Operator
import its.model.models.ClassModel
import org.apache.jena.rdf.model.Resource
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

//TODO сделать какие нибудь полезные обертки мб
data class Obj(
    val name: String,
    val resource: Resource,
)
typealias Clazz = ClassModel

data class EnumValue (
    val ownerEnum: String,
    val value: String,
)


object Types{
    /**
     * Объект
     */
    @JvmStatic
    val Object = Obj::class

    /**
     * Класс
     */
    @JvmStatic
    val Class = Clazz::class

    /**
     * Строка
     */
    @JvmStatic
    val String = String::class

    /**
     * Булево значение
     */
    @JvmStatic
    val Boolean = Boolean::class

    /**
     * Целое число
     */
    @JvmStatic
    val Integer = Int::class

    /**
     * Дробное число
     */
    @JvmStatic
    val Double = Double::class

    /**
     * Результат сравнения
     */
    @JvmStatic
    val ComparisonResult = ComparisonResult::class

    /**
     * Enum
     */
    @JvmStatic
    val Enum = EnumValue::class
    
    @JvmStatic
    val None = Unit::class



    /**
     * Может ли этот тип быть преобразован в другой
     * @param to Тип, в который преобразовываем
     * @return Может ли этот тип быть преобразован в другой
     */
    @JvmStatic
    fun KClass<*>.canCast(to: KClass<*>) : Boolean{
        return this == to //Сейчас так, потому что ушли от системы DataType
    }

    @JvmStatic
    fun KClass<*>.isValidType() : Boolean{
        return when(this){
            String, Boolean, Integer, Double, Enum, ComparisonResult, Class, Object -> true
            else -> false
        }
    }

    @JvmStatic
    fun Any.isOfValidType() : Boolean{
        return when(this){
            is String, is Boolean, is Int, is Double, is EnumValue, is ComparisonResult, is Clazz, is Obj -> true
            else -> false
        }
    }

    @JvmStatic
    fun Operator.fits(expectation: KClass<*>) : Boolean{
        return if(expectation.isSubclassOf(Operator::class)) this::class == expectation else this.resultDataType.canCast(expectation)
    }

    @JvmStatic
    fun typeFromString(value: String) = when (value.uppercase()) {
        "OBJECT" -> Object
        "CLASS" -> Class
        "STRING" -> String
        "BOOL","BOOLEAN" -> Boolean
        "INT","INTEGER" -> Integer
        "DOUBLE" -> Double
        "COMPARISON", "COMPARISONRESULT","COMPARISON_RESULT" -> ComparisonResult
        "ENUM" -> Enum
        else -> null
    }

    abstract class ValueBehaviour<Parameter, Returned> {
         protected fun Any.exec(param: Parameter) : Returned{
            require(this.isOfValidType()){
                "Recipient value of type ${this::class.simpleName} is not supported as a Type"
            }

            return when(this){
                is String -> this.exec(param)
                is Boolean -> this.exec(param)
                is Int -> this.exec(param)
                is Double -> this.exec(param)
                is EnumValue -> this.exec(param)
                is ComparisonResult -> this.exec(param)
                is Clazz -> this.exec(param)
                is Obj -> this.exec(param)
                else -> throw IllegalArgumentException("Recipient value of type ${this::class.simpleName} is not supported as a Type")
            }
        }


        protected abstract fun String.exec(param: Parameter): Returned
        protected abstract fun Boolean.exec(param: Parameter): Returned
        protected abstract fun Int.exec(param: Parameter): Returned
        protected abstract fun Double.exec(param: Parameter): Returned
        protected abstract fun EnumValue.exec(param: Parameter): Returned
        protected abstract fun ComparisonResult.exec(param: Parameter): Returned
        protected abstract fun Clazz.exec(param: Parameter): Returned
        protected abstract fun Obj.exec(param: Parameter): Returned
    }


    abstract class  TypeBehaviour<Parameter, Returned> {
        protected fun KClass<*>.exec(param: Parameter) : Returned{
            require(this.isValidType()){
                "Recipient type ${this.simpleName} is not supported as a Type"
            }

            return when(this){
                String -> forString(param)
                Boolean -> forBoolean(param)
                Integer -> forInt(param)
                Double -> forDouble(param)
                Enum -> forEnum(param)
                ComparisonResult -> forComparisonResult(param)
                Class -> forClazz(param)
                Object -> forObj(param)
                else -> throw IllegalArgumentException("Recipient type ${this.simpleName} is not supported as a Type")
            }
        }


        protected abstract fun forString(param: Parameter): Returned
        protected abstract fun forBoolean(param: Parameter): Returned
        protected abstract fun forInt(param: Parameter): Returned
        protected abstract fun forDouble(param: Parameter): Returned
        protected abstract fun forEnum(param: Parameter): Returned
        protected abstract fun forComparisonResult(param: Parameter): Returned
        protected abstract fun forClazz(param: Parameter): Returned
        protected abstract fun forObj(param: Parameter): Returned
    }
}