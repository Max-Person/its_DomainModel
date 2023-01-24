package its.model.models

/**
 * Модель переменной дерева рассуждения
 * @param name Имя переменной
 * @param className Имя класса переменной
 */
data class DecisionTreeVarModel(
    val name: String,
    val className: String
) {

    /**
     * Проверяет корректность модели
     * @throws IllegalArgumentException
     */
    fun validate() {
        require(name.isNotBlank()) {
            "Некорректное имя переменной."
        }
    }
}