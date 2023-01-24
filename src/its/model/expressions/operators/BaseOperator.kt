package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.util.DataType

/**
 * Базовый оператор
 */
abstract class BaseOperator(args: List<Operator>) : Operator {

    /**
     * Аргументы
     */
    final override val args: List<Operator>

    init {
        // Проверяем аргументы перед сохранением
        checkArgs(args)
        this.args = args
    }

    /**
     * Проверка аргументов
     * @param args Аргументы
     */
    private fun checkArgs(args: List<Operator>) {
        // Получаем список типов данных
        val actual: MutableList<DataType> = ArrayList()
        args.forEach {arg ->
            val resDataType = arg.resultDataType

            // Проверяем, что все операторы имеют возвращаемое значение
            requireNotNull(resDataType) { "Аргумент без возвращаемого значения" }

            actual.add(resDataType)
        }

        var success = false
        // Для каждого набора типа данных
        argsDataTypes.forEach { expected ->
            // Если количество аргументов неограниченно
            if (isArgsCountUnlimited) {
                // Проверяем, что кол-во полученных аргументов больше или равно ожидаемым
                if (expected.size <= actual.size) {
                    var equals = true
                    // Сравниваем аргументы
                    var i = 0
                    var j = 0
                    while (i < actual.size) {

                        // Если дошли до последнего аргумента - сравниваем все полученные с последним ожидаемым
                        if (j > expected.size - 1) {
                            j = expected.size - 1
                        }
                        // Аргументы совпадают - если типы данных равны или могут быть преобразованы
                        equals = equals && (actual[i] == expected[j]
                                || actual[i].canCast(expected[j]))
                        ++i
                        ++j
                    }
                    success = success || equals
                }
            } else {
                // Проверяем, что кол-во полученных аргументов равно ожидаемым
                if (expected.size == actual.size) {
                    var equals = true
                    // Сравниваем аргументы
                    for (i in actual.indices) {
                        // Аргументы совпадают - если типы данных равны или могут быть преобразованы
                        equals = equals && (actual[i] == expected[i]
                                || actual[i].canCast(expected[i]))
                    }
                    success = success || equals
                }
            }
        }

        // Если аргументы не совпали ни с одним набором - выбрасываем исключение
        require(success) { "Набор аргументов не соответствует ни одной из вариаций оператора" }
    }
}