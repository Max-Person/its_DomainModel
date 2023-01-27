package its.model.expressions.types

/**
 * Тип данных
 */
enum class DataType {

    /**
     * Переменная дерева мысли
     */
    DecisionTreeVar,

    /**
     * Класс
     */
    Class,

    /**
     * Объект
     */
    Object,

    /**
     * Свойство
     */
    Property,

    /**
     * Отношение
     */
    Relationship,

    /**
     * Строка
     */
    String,

    /**
     * Булево значение
     */
    Boolean,

    /**
     * Целое число
     */
    Integer,

    /**
     * Дробное число
     */
    Double,

    /**
     * Результат сравнения
     */
    ComparisonResult,

    /**
     * Enum
     */
    Enum;

    /**
     * Может ли этот тип быть преобразован в другой
     * @param to Тип, в который преобразовываем
     * @return Может ли этот тип быть преобразован в другой
     */
    fun canCast(to: DataType) = this == DecisionTreeVar && to == Object

    override fun toString() = when (this) {
        DecisionTreeVar -> "DECISION_TREE_VAR"
        Class -> "CLASS"
        Object -> "OBJECT"
        Property -> "PROPERTY"
        Relationship -> "RELATIONSHIP"
        String -> "STRING"
        Boolean -> "BOOLEAN"
        Integer -> "INTEGER"
        Double -> "DOUBLE"
        ComparisonResult -> "COMPARISON_RESULT"
        Enum -> "ENUM"
    }

    companion object _static{

        fun fromString(value: kotlin.String) = when (value.uppercase()) {
            "DECISIONTREEVAR","DECISION_TREE_VAR" -> DecisionTreeVar
            "CLASS" -> Class
            "OBJECT" -> Object
            "PROPERTY" -> Property
            "RELATIONSHIP" -> Relationship
            "STRING" -> String
            "BOOL","BOOLEAN" -> Boolean
            "INT","INTEGER" -> Integer
            "DOUBLE" -> Double
            "COMPARISON", "COMPARISONRESULT","COMPARISON_RESULT" -> ComparisonResult
            "ENUM" -> Enum
            else -> null
        }
    }
}