package its.model.dictionaries

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import its.model.dictionaries.util.DictionariesUtil.COLUMNS_SEPARATOR
import its.model.dictionaries.util.DictionariesUtil.LIST_ITEMS_SEPARATOR
import its.model.models.EnumModel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Словарь перечислений
 */
object EnumsDictionary {

    // +++++++++++++++++++++++++++++++++ Свойства ++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Список перечислений
     */
    private val enums: MutableList<EnumModel> = mutableListOf()

    /**
     * Названия предикатов, задающих нумерацию для шкал
     *
     * key - имя перечисления,
     * val - имя предиката нумерации
     */
    private val scalePredicates: MutableMap<String, String> = HashMap()

    /**
     * ID предиката
     */
    private var scalePredicateId = 0
        get() = ++field


    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Инициализирует словарь данными
     * @param path Путь с фалу с данными для словаря
     */
    internal fun init(path: String) {
        // Очищаем старые значения
        enums.clear()

        // Создаем объекты
        val parser = CSVParserBuilder().withSeparator(COLUMNS_SEPARATOR).build()
        val bufferedReader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)
        val csvReader = CSVReaderBuilder(bufferedReader).withCSVParser(parser).build()

        // Считываем файл
        csvReader.use { reader ->
            val rows = reader.readAll()

            rows.forEach { row ->
                val name = row[0]
                val values = row[1].split(LIST_ITEMS_SEPARATOR).filter { it.isNotBlank() }
                val isLinear = row[2].toBoolean()
                val linearPredicate = row[3].ifBlank { null }

                require(!exist(name)) {
                    "Перечисление $name уже объявлено в словаре."
                }
                require(!isLinear || linearPredicate != null) {
                    "Для линейного перечисления $name не указан линейный предикат."
                }

                enums.add(
                    EnumModel(
                        name = name,
                        values = values,
                        isLinear = isLinear
                    )
                )
            }
        }
    }

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    internal fun forEach(block: (EnumModel) -> Unit) {
        enums.forEach(block)
    }

    /**
     * Получить предикат линейной шкалы для перечисления
     * @param name Имя перечисления
     */
    internal fun getScalePredicate(name: String) = scalePredicates[name]

    /**
     * Получить модель перечисления по имени
     * @param name Имя перечисления
     */
    internal fun get(name: String) = enums.firstOrNull { it.name == name }

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    fun validate() {
        enums.forEach {
            it.validate()
        }
    }

    /**
     * Существует ли перечисление
     * @param name Имя перечисления
     */
    fun exist(name: String) = enums.any { it.name == name }

    /**
     * Содержит ли перечисление указанное значение
     * @param name Имя перечисления
     * @param value Значение
     */
    fun containsValue(name: String, value: String) = get(name)?.containsValue(value)

    /**
     * Получить список всех значений перечисления
     * @param name Имя перечисления
     * @return Список всех значений
     */
    fun values(name: String) = get(name)?.values
}