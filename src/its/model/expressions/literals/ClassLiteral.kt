package its.model.expressions.literals

import its.model.DomainModel
import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.expressions.visitors.LiteralBehaviour

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

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}