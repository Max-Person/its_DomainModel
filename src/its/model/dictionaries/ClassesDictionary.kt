package its.model.dictionaries

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import its.model.dictionaries.DictionariesUtil.COLUMNS_SEPARATOR
import its.model.models.ClassModel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Словарь классов
 */
object ClassesDictionary {

    // +++++++++++++++++++++++++++++++++ Свойства ++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Список классов
     */
    private val classes: MutableList<ClassModel> = mutableListOf()

    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Инициализирует словарь данными
     * @param path Путь с фалу с данными для словаря
     */
    internal fun init(path: String) {
        // Очищаем старые значения
        classes.clear()

        // Создаем объекты
        val parser = CSVParserBuilder().withSeparator(COLUMNS_SEPARATOR).build()
        val bufferedReader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)
        val csvReader = CSVReaderBuilder(bufferedReader).withCSVParser(parser).build()

        // Считываем файл
        csvReader.use { reader ->
            val rows = reader.readAll()

            rows.forEach { row ->
                val name = row[0]
                val parent = row[1].ifBlank { null }
                val calcExprXML = row[2].ifBlank { null }

                require(!exist(name)) {
                    "Класс $name уже объявлен в словаре."
                }

                classes.add(
                    ClassModel(
                        name = name,
                        parent = parent,
                        calcExprXML = calcExprXML
                    )
                )
            }
        }
    }

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Получить модель класса по имени
     * @param name Имя класса
     */
    internal fun get(name: String) = classes.firstOrNull { it.name == name }

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    fun validate() {
        classes.forEach {
            it.validate()
            require(it.parent == null || exist(it.parent)) {
                "Класс ${it.parent} не объявлен в словаре."
            }
        }
    }

    /**
     * Существует ли класс
     * @param name Имя класса
     */
    fun exist(name: String) = classes.any { it.name == name }

    /**
     * Получить имя родительского класса
     * @param name Имя класса
     */
    fun parent(name: String) = get(name)?.parent

    /**
     * Является ли класс родителем другого
     * @param child Ребенок
     * @param parent Родитель
     */
    fun isParentOf(child: String, parent: String): Boolean {
        if (!exist(child) || !exist(parent) || parent(child) == null) return false
        return if (parent(child) == parent) true else isParentOf(parent(child)!!, parent)
    }

    /**
     * Вычисляемый ли класс
     * @param name Имя класса
     */
    fun isCalculable(name: String) = get(name)?.calcExpr != null

    /**
     * Получить выражение для вычисления класса
     * @param name Имя класса
     * @return Выражение для вычисления
     */
    fun calcExpr(name: String) = get(name)?.calcExpr
}