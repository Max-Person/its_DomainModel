package its.model.expressions.literals

import its.model.DomainModel
import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType

/**
 * Class литерал
 * @param value Имя класса
 */
class ClassLiteral(value: String) : Literal(value) {

    init {
        // Проверяем существование класса
        require(DomainModel.classesDictionary.exist(value)) { "Класс $value не объявлен в словаре." }
    }

    override val resultDataType: DataType
        get() = DataType.Class

    override fun clone(): Operator = ClassLiteral(value)
}