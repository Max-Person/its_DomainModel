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

abstract class DictionaryBase<T : Any>(path: String, storedType: KClass<T>) {

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

        @JvmField
        val valueParser = TypeParser.newBuilder().setSplitStrategy { s: String, h: SplitStrategyHelper ->
            s.split(LIST_ITEMS_SEPARATOR)
        }.setNullStringStrategy{ s: String, nullStringStrategyHelper: NullStringStrategyHelper -> s.equals("")}.build()

        @JvmField
        val csvParser = CSVParserBuilder().withSeparator(COLUMNS_SEPARATOR).build()
    }

    init {
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

                onAddValidation(value, storedType)

                values.add(value)
            }
        }
    }

    /**
     * Валидация добавляемого в словарь элемента.
     * Выполняется после парсинга и создания элемента, но до его добавления в словарь
     * @param value созданный элемент, требующий проверки
     * @throws IllegalArgumentException
     */
    protected abstract fun onAddValidation(value : T, stored: KClass<T>)

    /**
     * Дополнительные действия, которые необходимо выполнить при добавлении элемента в словарь.
     * Выполняется после добавления элемента в словарь
     * @param added созданный элемент, требующий проверки
     * @throws IllegalArgumentException
     */
    protected abstract fun onAddActions(added : T, stored: KClass<T>)

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