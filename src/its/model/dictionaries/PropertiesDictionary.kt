package its.model.dictionaries

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import its.model.dictionaries.DictionariesUtil.COLUMNS_SEPARATOR
import its.model.dictionaries.DictionariesUtil.LIST_ITEMS_SEPARATOR
import its.model.expressions.types.DataType
import its.model.models.PropertyModel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Словарь свойств
 */
object PropertiesDictionary {

    // +++++++++++++++++++++++++++++++++ Константы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Разделитель возможных значений у свойств
     */
    private const val RANGE_SEPARATOR = '-'

    // +++++++++++++++++++++++++++++++++ Свойства ++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Список свойств
     */
    private val properties: MutableList<PropertyModel> = mutableListOf()

    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Инициализирует словарь данными
     * @param path Путь с фалу с данными для словаря
     */
    internal fun init(path: String) {
        // Очищаем старые значения
        properties.clear()

        // Создаем объекты
        val parser = CSVParserBuilder().withSeparator(COLUMNS_SEPARATOR).build()
        val bufferedReader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)
        val csvReader = CSVReaderBuilder(bufferedReader).withCSVParser(parser).build()

        // Считываем файл
        csvReader.use { reader ->
            val rows = reader.readAll()

            rows.forEach { row ->
                val name = row[0]
                val dataType = DataType.valueOf(row[1])
                val enumName = row[2].ifBlank { null }
                val isStatic = row[3].toBoolean()
                val owners = row[4]
                    .split(LIST_ITEMS_SEPARATOR)
                    .filter { it.isNotBlank() }
                    .ifEmpty { null }
                val valuesRanges = row[5]
                    .split(LIST_ITEMS_SEPARATOR)
                    .filter { it.isNotBlank() }
                    .map {
                        Pair(
                            it.split(RANGE_SEPARATOR)[0].toDouble(),
                            it.split(RANGE_SEPARATOR)[1].toDouble()
                        )
                    }
                    .ifEmpty { null }

                require(!exist(name)) {
                    "Свойство $name уже объявлено в словаре."
                }
                require(dataType != null) {
                    "Некорректный тип данных ${row[1]}."
                }
                require(isStatic == !owners.isNullOrEmpty()) {
                    if (isStatic) {
                        "Свойством $name не обладает ни один класс."
                    } else {
                        "У нестатического свойства $name не должно быть классов-владельцев."
                    }
                }

                properties.add(
                    PropertyModel(
                        name = name,
                        dataType = dataType,
                        enumName = enumName,
                        owners = owners,
                        valuesRanges = valuesRanges
                    )
                )
            }
        }
    }

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Получить модель свойства по имени
     * @param name Имя свойства
     */
    internal fun get(name: String) = properties.firstOrNull { it.name == name }

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    fun validate() {
        properties.forEach {
            it.validate()
            require(it.dataType != DataType.Enum || it.enumName != null && EnumsDictionary.exist(it.enumName)) {
                "Для свойства ${it.name} не указано имя перечисления, или оно не объявлено в словаре."
            }
            it.owners?.forEach { owner ->
                require(ClassesDictionary.exist(owner)) {
                    "Класс $owner не объявлен в словаре."
                }
            }
        }
    }

    /**
     * Существует ли свойство
     * @param name Имя свойства
     */
    fun exist(name: String) = properties.any { it.name == name }

    /**
     * Является ли статическим
     * @param name Имя свойства
     */
    fun isStatic(name: String) = get(name)?.isStatic

    /**
     * Получить имя перечисления свойства
     * @param name Имя свойства
     * @return Имя перечисления
     */
    fun enumName(name: String) = get(name)?.enumName

    /**
     * Тип данных свойства
     * @param name Имя свойства
     */
    fun dataType(name: String) = get(name)?.dataType

    /**
     * Переопределяется ли свойство
     * @param name Имя свойства
     */
    fun isPropertyBeingOverridden(name: String): Boolean {
        if (!exist(name)) return false
        if (isStatic(name) != true) return false

        val classes = get(name)?.owners!!
        for (i in classes.indices) {
            for (j in classes.indices) {
                if (i == j) continue
                if (ClassesDictionary.isParentOf(classes[i], classes[j])) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Попадает ли значение в один из диапазонов свойства
     * @param name Имя свойства
     * @param value Значение
     */
    fun isValueInRange(name: String, value: Int) = get(name)?.isValueInRange(value)

    /**
     * Попадает ли значение в один из диапазонов свойства
     * @param name Имя свойства
     * @param value Значение
     */
    fun isValueInRange(name: String, value: Double) = get(name)?.isValueInRange(value)
}