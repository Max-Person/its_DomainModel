package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.Types.fits

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
        var success = false
        // Для каждого набора типа данных
        argsDataTypes.forEach { expected ->
            // Если количество аргументов неограниченно
            if (isArgsCountUnlimited) {
                // Проверяем, что кол-во полученных аргументов больше или равно ожидаемым
                if (expected.size <= args.size) {
                    var equals = true
                    // Сравниваем аргументы
                    var i = 0
                    var j = 0
                    while (i < args.size) {

                        // Если дошли до последнего аргумента - сравниваем все полученные с последним ожидаемым
                        if (j > expected.size - 1) {
                            j = expected.size - 1
                        }
                        // Аргументы совпадают - если типы данных равны или могут быть преобразованы
                        equals = equals && args[i].fits(expected[j])
                        ++i
                        ++j
                    }
                    success = success || equals
                }
            } else {
                // Проверяем, что кол-во полученных аргументов равно ожидаемым
                if (expected.size == args.size) {
                    var equals = true
                    // Сравниваем аргументы
                    for (i in args.indices) {
                        // Аргументы совпадают - если типы данных равны или могут быть преобразованы
                        equals = equals && args[i].fits(expected[i])
                    }
                    success = success || equals
                }
            }
        }

        // Если аргументы не совпали ни с одним набором - выбрасываем исключение
        require(success) { "Набор аргументов не соответствует ни одной из вариаций оператора" }
    }
}