package its.model.util

/**
 * Генерирует уникальные идентификаторы для различных элементов правил
 */
object NamingManager {

    /**
     * Символы, которые добавляются в генерируемые имена для защиты от совпадений с пользовательскими
     */
    internal const val PROTECTIVE_CHARS = "..."

    /**
     * Индекс для переменных
     */
    private var varIndex = 0
        get() = ++field

    /**
     * Индекс для предикатов
     */
    private var predicateIndex = 0
        get() = ++field

    /**
     * Генерирует уникальное имя для переменной, не совпадающее с пользовательскими именами переменных
     * @return Имя переменной
     */
    fun genVarName() = JenaUtil.genVar("var$varIndex$PROTECTIVE_CHARS")

    /**
     * Генерирует уникальное имя для предиката, не совпадающее с пользовательскими именами предикатов
     * @return Имя предиката
     */
    fun genPredicateName() = JenaUtil.genLink(JenaUtil.POAS_PREF, "predicate$predicateIndex$PROTECTIVE_CHARS")
}