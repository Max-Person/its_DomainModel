package its.model.dictionaries

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import its.model.dictionaries.util.DictionariesUtil.COLUMNS_SEPARATOR
import its.model.models.DecisionTreeVarModel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

// TODO: вычислять по дереву
/**
 * Словарь переменных дерева мысли
 */
object DecisionTreeVarsDictionary {

    // +++++++++++++++++++++++++++++++++ Свойства ++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Список переменных дерева мысли
     */
    private val decisionTreeVars: MutableList<DecisionTreeVarModel> = mutableListOf()

    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Инициализирует словарь данными
     * @param path Путь с фалу с данными для словаря
     */
    internal fun init(path: String) {
        // Очищаем старые значения
        decisionTreeVars.clear()

        // Создаем объекты
        val parser = CSVParserBuilder().withSeparator(COLUMNS_SEPARATOR).build()
        val bufferedReader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)
        val csvReader = CSVReaderBuilder(bufferedReader).withCSVParser(parser).build()

        // Считываем файл
        csvReader.use { reader ->
            val rows = reader.readAll()

            rows.forEach { row ->
                val name = row[0]
                val className = row[1]

                require(!exist(name)) {
                    "Переменная $name уже объявлена в словаре."
                }

                decisionTreeVars.add(
                    DecisionTreeVarModel(
                        name = name,
                        className = className
                    )
                )
            }
        }
    }

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Получить модель переменной по имени
     * @param name Имя переменной
     */
    internal fun get(name: String) = decisionTreeVars.firstOrNull { it.name == name }

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    fun validate() {
        decisionTreeVars.forEach {
            it.validate()
            require(ClassesDictionary.exist(it.className)) {
                "Класс ${it.className} не объявлен в словаре."
            }
        }
    }

    /**
     * Существует ли переменная с указанным именем
     * @param name Имя переменной
     */
    fun exist(name: String) = decisionTreeVars.any { it.name == name }

    /**
     * Получить класс переменной дерева мысли
     * @param name Имя переменной
     * @return Имя класса переменной
     */
    fun getClass(name: String) = get(name)?.className
}