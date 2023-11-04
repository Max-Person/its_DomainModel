package its.model.dictionaries

import com.github.drapostolos.typeparser.NullStringStrategyHelper
import com.github.drapostolos.typeparser.ParserHelper
import com.github.drapostolos.typeparser.SplitStrategyHelper
import com.github.drapostolos.typeparser.TypeParser
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import its.model.expressions.types.ComparisonResult
import its.model.expressions.types.Types.typeFromString
import its.model.models.ContinuousRange
import its.model.models.DiscreteRange
import its.model.models.Range
import its.model.models.RelationshipModel
import java.io.Reader
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor

abstract class DictionaryBase<T : Any>(protected val storedType: KClass<T>) : Iterable<T> {

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
        const val RANGE_SEPARATOR = '-'

        private val valueParser = TypeParser.newBuilder().setSplitStrategy { s: String, h: SplitStrategyHelper ->
            s.split(LIST_ITEMS_SEPARATOR)
        }.registerParser(KClass::class.java) { s: String, h: ParserHelper ->
            typeFromString(s)
        }.registerParser(RelationshipModel.ScaleType::class.java) { s: String, h: ParserHelper ->
            RelationshipModel.ScaleType.fromString(s)
        }.registerParser(RelationshipModel.RelationType::class.java) { s: String, h: ParserHelper ->
            RelationshipModel.RelationType.fromString(s)
        }.registerParser(ComparisonResult::class.java) { s: String, h: ParserHelper ->
            ComparisonResult.fromString(s)
        }.registerParser(Range::class.java) { s: String, h: ParserHelper ->
            if (h.isNullString(s))
                return@registerParser null
            val discrete = h.split(s)
            if (discrete.size == 1 && discrete[0].contains(RANGE_SEPARATOR)) {
                return@registerParser ContinuousRange(
                    discrete[0].split(RANGE_SEPARATOR, ignoreCase = true, limit = 2)
                        .run { this[0].toDouble() to this[1].toDouble() })
            } else
                return@registerParser DiscreteRange(discrete.map { it.toDouble() })
        }.setNullStringStrategy { s: String, nullStringStrategyHelper: NullStringStrategyHelper -> s.equals("") }
            .build()

        private val csvParser = CSVParserBuilder().withSeparator(COLUMNS_SEPARATOR).withEscapeChar('\\').build()
    }

    private var isInit = false

    /**
     * Инициализация словаря из файла .csv.
     *
     * *Важно:* функции инициализации не могут быть вызваны повторно на одном объекте
     * @param path путь к файлу словаря
     */
    fun fromCSV(reader: Reader): DictionaryBase<T> {
        require(!isInit) {
            "Функция инициализации словаря не может быть вызвана повторно"
        }

        val csvReader = CSVReaderBuilder(reader).withCSVParser(csvParser).build()
        val constructor = storedType.primaryConstructor?.javaConstructor!!
        csvReader.use { reader ->
            val rows = reader.readAll()

            rows.forEach { row ->
                require(row.size >= constructor.parameterCount) {
                    "${this.javaClass.simpleName}'s rows must have at least ${constructor.parameterCount} fields (row ${
                        rows.indexOf(
                            row
                        )
                    })"
                }
                val args = constructor.genericParameterTypes.mapIndexed { index, type ->
                    valueParser.parseType(row[index].trim(), if (type is ParameterizedType) type.rawType else type)
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
    protected fun add(value: T) {
        onAddValidation(value)
        values.add(value)
        onAddActions(value)
    }

    /**
     * Добавление нескольких элементов в словарь.
     * @see add
     * @param values добавляемые элементы
     */
    protected fun addAll(values: Collection<T>) {
        values.forEach { add(it) }
    }

    /**
     * Валидация добавляемого в словарь элемента.
     * Выполняется после парсинга и создания элемента, но до его добавления в словарь
     * @param value созданный элемент, требующий проверки
     * @throws IllegalArgumentException
     */
    protected abstract fun onAddValidation(value: T)

    /**
     * Дополнительные действия, которые необходимо выполнить при добавлении элемента в словарь.
     * Выполняется после добавления элемента в словарь
     * @param added созданный элемент, для которого выполняются дополнительные действия
     * @throws IllegalArgumentException
     */
    protected abstract fun onAddActions(added: T)

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    abstract fun validate()

    abstract fun get(name: String): T?

    override fun iterator(): Iterator<T> {
        return values.iterator()
    }

    fun get(predicate: (T) -> Boolean): T? {
        return values.firstOrNull(predicate)
    }

    fun contains(name: String): Boolean {
        return get(name) != null
    }

    fun forEach(block: (T) -> Unit) {
        values.forEach(block)
    }
}