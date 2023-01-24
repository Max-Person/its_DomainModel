package its.model.expressions.types

/**
 * Описывает результат сравнения
 */
sealed class ComparisonResult {

    /**
     * Больше
     */
    object Greater : ComparisonResult()

    /**
     * Меньше
     */
    object Less : ComparisonResult()

    /**
     * Эквивалентно
     */
    object Equal : ComparisonResult()

    /**
     * Неэквивалентно
     */
    object NotEqual : ComparisonResult()

    /**
     * Не определено
     */
    object Undetermined : ComparisonResult()

    override fun toString() = when (this) {
        Greater -> "GREATER"
        Less -> "LESS"
        Equal -> "EQUAL"
        NotEqual -> "NOT_EQUAL"
        Undetermined -> "UNDETERMINED"
    }

    companion object {

        fun valueOf(value: String) = when (value) {
            "GREATER" -> Greater
            "LESS" -> Less
            "EQUAL" -> Equal
            "NOT_EQUAL" -> NotEqual
            "UNDETERMINED" -> Undetermined
            else -> null
        }
    }
}