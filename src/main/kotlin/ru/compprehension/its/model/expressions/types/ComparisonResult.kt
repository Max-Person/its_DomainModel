package ru.compprehension.its.model.expressions.types

/**
 * Описывает результат сравнения
 */
enum class ComparisonResult {

    /**
     * Больше
     */
    Greater,

    /**
     * Меньше
     */
    Less,

    /**
     * Эквивалентно
     */
    Equal,

    /**
     * Неэквивалентно
     */
    NotEqual,

    /**
     * Не определено
     */
    Undetermined;

    override fun toString() = when (this) {
        Greater -> "GREATER"
        Less -> "LESS"
        Equal -> "EQUAL"
        NotEqual -> "NOT_EQUAL"
        Undetermined -> "UNDETERMINED"
    }

    companion object _static {
        @JvmStatic
        fun fromString(value: String) = when (value.uppercase()) {
            "GREATER" -> Greater
            "LESS" -> Less
            "EQUAL" -> Equal
            "NOTEQUAL", "NOT_EQUAL" -> NotEqual
            "UNDETERMINED" -> Undetermined
            else -> null
        }
    }
}