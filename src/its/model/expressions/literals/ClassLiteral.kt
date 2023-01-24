package its.model.expressions.literals

import its.model.dictionaries.ClassesDictionary
import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.util.DataType

/**
 * Class литерал
 * @param value Имя класса
 */
class ClassLiteral(value: String) : Literal(value) {

    init {
        // Проверяем существование класса
        require(ClassesDictionary.exist(value)) { "Класс $value не объявлен в словаре." }
    }

    override val resultDataType: DataType
        get() = DataType.Class

    override fun clone(): Operator = ClassLiteral(value)
}