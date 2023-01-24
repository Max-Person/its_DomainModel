package its.model.util

import its.model.util.NamingManager.PROTECTIVE_CHARS
import its.model.util.NamingManager.genPredicateName

/**
 * Содержит различные утилитарные методы и переменные, используемые при генерации правил
 */
object JenaUtil {

    // +++++++++++++++++++++++++++++++++ Префиксы ++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // TODO?: пользовательские префиксы?

    /**
     * POAS префикс
     */
    const val POAS_PREF = "http://www.vstu.ru/poas/code#"

    /**
     * XSD префикс
     */
    const val XSD_PREF = "http://www.w3.org/2001/XMLSchema#"

    /**
     * RDF префикс
     */
    const val RDF_PREF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"

    /**
     * RDFS префикс
     */
    const val RDFS_PREF = "http://www.w3.org/2000/01/rdf-schema#"

    // ++++++++++++++++++++++++ Вспомогательные правила ++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    val AUXILIARY_RULES = """
        [makeSkolem(?tmp) -> (?tmp ${genPredicateName()} "true"^^${XSD_PREF}boolean)]
        [(?a ?p ?b), (?p ${RDFS_PREF}domain ?c) -> (?a ${RDF_PREF}type ?c)]
        [(?a ?p ?b), (?p ${RDFS_PREF}range ?c) -> (?b ${RDF_PREF}type ?c)]
        [(?a ?p ?b), (?p ${RDFS_PREF}subPropertyOf ?q) -> (?a ?q ?b)]
        [(?a ${RDFS_PREF}subClassOf ?b), (?b ${RDFS_PREF}subClassOf ?c) -> (?a ${RDFS_PREF}subClassOf ?c)]
        [(?a ${RDFS_PREF}subClassOf ?b), (?c ${RDF_PREF}type ?a) -> (?c ${RDF_PREF}type ?b)]
    """.trimIndent()

    // +++++++++++++++++++++++++++++++++ Константы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Предикат переменной дерева мысли
     */
    const val DECISION_TREE_VAR_PREDICATE = "var$PROTECTIVE_CHARS"

    /**
     * Предикат результата сравнения
     */
    const val COMPARE_RESULT_PREDICATE = "compareResult$PROTECTIVE_CHARS"

    /**
     * Маркировка паузы
     */
    const val PAUSE_MARK = "<pause>"

    // +++++++++++++++++++++++++++++++++ Шаблоны +++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Шаблон триплета правила
     */
    private const val TRIPLE_PATTERN = "(<subj> <predicate> <obj>)\n"

    /**
     * Основной шаблон правила с возвращаемым значением
     */
    private const val MAIN_RULE_PATTERN =
        "[\n<ruleHead>makeSkolem(<skolemName>)\n->\n(<skolemName> <resPredicateName> <resVarName>)\n]\n\n"

    /**
     * Шаблон для boolean правила
     */
    private const val BOOLEAN_RULE_PATTERN =
        "[\n<ruleHead>makeSkolem(<skolemName>)\n->\n(<skolemName> <resPredicateName> \"true\"^^xsd:boolean)\n]\n\n"

    // ++++++++++++++++++++++++++++ Методы для генерации +++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Сгенерировать строковую константу
     * @param value Строковая константа
     * @return Запись строковой константы для правила
     */
    fun genVal(value: String) = "\"$value\"^^${XSD_PREF}string"

    /**
     * Сгенерировать булеву константу
     * @param value Булева константа
     * @return Запись булевой константы для правила
     */
    fun genVal(value: Boolean) = "\"$value\"^^${XSD_PREF}boolean"

    /**
     * Сгенерировать целочисленную константу
     * @param value Целочисленная константа
     * @return Запись целочисленной константы для правила
     */
    fun genVal(value: Int) = "\"$value\"^^${XSD_PREF}integer"

    /**
     * Сгенерировать дробную константу
     * @param value Дробная константа
     * @return Запись дробной константы для правила
     */
    fun genVal(value: Double) = "\"$value\"^^${XSD_PREF}double"

    /**
     * Сгенерировать ссылку в правиле
     * @param pref Префикс
     * @param obj Имя
     * @return Ссылка в правиле
     */
    fun genLink(pref: String, obj: String) = "$pref$obj"

    /**
     * Сгенерировать переменную
     * @param name Имя переменной
     * @return Имя переменной для правила
     */
    fun genVar(name: String) = "?$name"

    /**
     * Сгенерировать примитив, проверяющий эквивалентность
     * @param first Первый операнд
     * @param second Второй операнд
     * @return Примитив, проверяющий эквивалентность
     */
    fun genEqualPrim(first: String, second: String) = "equal($first,$second)\n"

    /**
     * Сгенерировать примитив, проверяющий неэквивалентность
     * @param first Первый операнд
     * @param second Второй операнд
     * @return Примитив, проверяющий неэквивалентность
     */
    fun genNotEqualPrim(first: String, second: String) = "notEqual($first,$second)\n"

    /**
     * Сгенерировать примитив, проверяющий, что первый операнд меньше второго
     * @param first Первый операнд
     * @param second Второй операнд
     * @return Примитив, проверяющий, что первый операнд меньше второго
     */
    fun genLessThanPrim(first: String, second: String) = "lessThan($first,$second)\n"

    /**
     * Сгенерировать примитив, проверяющий, что первый операнд больше второго
     * @param first Первый операнд
     * @param second Второй операнд
     * @return Примитив, проверяющий, что первый операнд больше второго
     */
    fun genGreaterThanPrim(first: String, second: String) = "greaterThan($first,$second)\n"

    /**
     * Сгенерировать примитив, проверяющий, что первый операнд меньше второго или равен ему
     * @param first Первый операнд
     * @param second Второй операнд
     * @return Примитив, проверяющий, что первый операнд меньше второго или равен ему
     */
    fun genLessEqualPrim(first: String, second: String) = "le($first,$second)\n"

    /**
     * Сгенерировать примитив, проверяющий, что первый операнд больше второго или равен ему
     * @param first Первый операнд
     * @param second Второй операнд
     * @return Примитив, проверяющий, что первый операнд больше второго или равен ему
     */
    fun genGreaterEqualPrim(first: String, second: String) = "ge($first,$second)\n"

    /**
     * Сгенерировать примитив, проверяющий отсутствие у объекта указанного предиката
     * @param subj Субъект
     * @param predicate Предикат
     * @return Примитив, проверяющий отсутствие у объекта указанной предиката
     */
    fun genNoValuePrim(subj: String, predicate: String) = "noValue($subj,$predicate)\n"

    /**
     * Сгенерировать примитив, проверяющий отсутствие у объекта указанного предиката с указанным значением
     * @param subj Субъект
     * @param predicate Предикат
     * @param obj Объект
     * @return Примитив, проверяющий отсутствие у объекта указанной предиката с указанным значением
     */
    fun genNoValuePrim(subj: String, predicate: String, obj: String) = "noValue($subj,$predicate,$obj)\n"

    /**
     * Сгенерировать примитив, создающий сколем с указанным именем
     * @param skolemName Имя
     * @return Примитив, создающий сколем с указанным именем
     */
    fun genMakeSkolemPrim(skolemName: String) = "makeSkolem($skolemName)\n"

    /**
     * Сгенерировать примитив, записывающий значение одной переменной в другую
     * @param from Имя переменной, из которой берется значение
     * @param to Имя переменной, в которую записывается значение
     * @return Примитив, записывающий значение одной переменной в другую
     */
    fun genBindPrim(from: String, to: String) = "bind($from,$to)\n"

    /**
     * Сгенерировать примитив, подсчитывающий количество связанных объектов
     * @param obj Объект
     * @param predicate Предикат
     * @param res Результат
     * @return Примитив, подсчитывающий количество связанных объектов
     */
    fun genCountValuesPrim(obj: String, predicate: String, res: String) = "countValues($obj,$predicate,$res)\n"

    /**
     * Сгенерировать триплет правила
     * @param subj Субъект
     * @param predicate Предикат
     * @param obj Объект
     * @return Триплет правила
     */
    fun genTriple(subj: String, predicate: String, obj: String): String {
        var res = TRIPLE_PATTERN
        res = res.replace("<subj>", subj)
        res = res.replace("<predicate>", predicate)
        res = res.replace("<obj>", obj)
        return res
    }

    /**
     * Сгенерировать правило с возвращаемым значением
     * @param ruleHead Голова правила
     * @param skolemName Имя сколем
     * @param resPredicateName Предикат, указывающий на результат
     * @param resVarName Переменная, содержащая результат
     * @return Правило
     */
    fun genRule(ruleHead: String, skolemName: String, resPredicateName: String, resVarName: String): String {
        var rule = MAIN_RULE_PATTERN
        rule = rule.replace("<ruleHead>", ruleHead)
        rule = rule.replace("<skolemName>", skolemName)
        rule = rule.replace("<resPredicateName>", resPredicateName)
        rule = rule.replace("<resVarName>", resVarName)
        return rule
    }

    /**
     * Сгенерировать булево правило
     * @param ruleHead Голова правила
     * @param skolemName Имя сколем
     * @param resPredicateName Предикат, являющийся флагом результата
     * @return Правило
     */
    fun genBooleanRule(ruleHead: String, skolemName: String, resPredicateName: String): String {
        var rule = BOOLEAN_RULE_PATTERN
        rule = rule.replace("<ruleHead>", ruleHead)
        rule = rule.replace("<skolemName>", skolemName)
        rule = rule.replace("<resPredicateName>", resPredicateName)
        return rule
    }
}