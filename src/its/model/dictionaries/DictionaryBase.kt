package its.model.dictionaries

import com.github.drapostolos.typeparser.NullStringStrategyHelper
import com.github.drapostolos.typeparser.SplitStrategyHelper
import com.github.drapostolos.typeparser.TypeParser
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor

abstract class DictionaryBase<T : Any>(protected val storedType: KClass<T>) {

    // +++++++++++++++++++++++++++++++++ Свойства ++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Список значений в словаре
     */
    protected val values: MutableList<T> = mutableListOf()

    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    private companion object _static {
        /**
         * Разделитель столбцов в CSV файле словаря
         */
        const val COLUMNS_SEPARATOR = '|'

        /**
         * Разделитель элементов списка в ячейке CSV файла словаря
         */
        const val LIST_ITEMS_SEPARATOR = ';'

        /**
         * Разделитель возможных значений у свойств
         */
        private const val RANGE_SEPARATOR = '-'

        private val valueParser = TypeParser.newBuilder().setSplitStrategy { s: String, h: SplitStrategyHelper ->
            s.split(LIST_ITEMS_SEPARATOR)
        }.setNullStringStrategy{ s: String, nullStringStrategyHelper: NullStringStrategyHelper -> s.equals("")}.build()

        private val csvParser = CSVParserBuilder().withSeparator(COLUMNS_SEPARATOR).build()
    }

    private var isInit = false

    /**
     * Инициализация словаря из файла .csv.
     *
     * *Важно:* функции инициализации не могут быть вызваны повторно на одном объекте
     * @param path путь к файлу словаря
     */
    fun fromCSV(path: String) : DictionaryBase<T> {
        require(!isInit){
            "Функция инициализации словаря не может быть вызвана повторно"
        }

        val bufferedReader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)
        val csvReader = CSVReaderBuilder(bufferedReader).withCSVParser(csvParser).build()
        csvReader.use { reader ->
            val rows = reader.readAll()

            rows.forEach { row ->
                val constructor = storedType.primaryConstructor?.javaConstructor!!
                val args = row.mapIndexed { index, s ->
                    valueParser.parseType(s, constructor.genericParameterTypes[index])
                }
                val value = constructor.newInstance(*args.toTypedArray())

                add(value)
            }
        }
        isInit = true
        return this
    }

    /**
     * Добавление элемента в словарь.
     * Перед добавлением элемент валидируется с помощью onAddValidation.
     * После добавления элемента выполняются дополнительные действия onAddActions
     * @param value добавляемый элемент
     */
    protected fun add(value: T){
        onAddValidation(value)
        values.add(value)
        onAddActions(value)
    }

    /**
     * Добавление нескольких элементов в словарь.
     * @see add
     * @param values добавляемые элементы
     */
    protected fun addAll(values : Collection<T>){
        values.forEach {add(it)}
    }

    /**
     * Валидация добавляемого в словарь элемента.
     * Выполняется после парсинга и создания элемента, но до его добавления в словарь
     * @param value созданный элемент, требующий проверки
     * @throws IllegalArgumentException
     */
    protected abstract fun onAddValidation(value : T)

    /**
     * Дополнительные действия, которые необходимо выполнить при добавлении элемента в словарь.
     * Выполняется после добавления элемента в словарь
     * @param added созданный элемент, для которого выполняются дополнительные действия
     * @throws IllegalArgumentException
     */
    protected abstract fun onAddActions(added : T)

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    abstract fun validate()

    fun forEach(block: (T) -> Unit) {
        values.forEach(block)
    }
}