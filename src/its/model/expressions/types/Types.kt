package its.model.expressions.types

import its.model.expressions.Operator
import org.apache.jena.rdf.model.Resource
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

//TODO сделать какие нибудь полезные обертки мб
data class Obj(
    val name: String,
    val resource: Resource,
)
data class Clazz(
    val name: String,
    val resource: Resource,
)

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
}