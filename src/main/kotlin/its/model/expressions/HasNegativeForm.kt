package its.model.expressions

interface HasNegativeForm {

    /**
     * Является ли негативным
     */
    fun isNegative(): Boolean

    /**
     * Установить состояние негативности
     */
    fun setIsNegative(isNegative: Boolean)
}