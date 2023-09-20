package ru.compprehension.its.model.expressions.literals

import ru.compprehension.its.model.DomainModel
import ru.compprehension.its.model.expressions.Operator
import ru.compprehension.its.model.expressions.types.Types
import ru.compprehension.its.model.expressions.visitors.LiteralBehaviour

/**
 * Class литерал
 * @param name Имя класса
 */
class ClassRef(name: String) : ReferenceLiteral(name) {

    init {
        // Проверяем существование класса
        require(DomainModel.classesDictionary.exist(name)) { "Класс $name не объявлен в словаре." }
    }

    override val resultDataType
        get() = Types.Class

    override fun clone(): Operator = ClassRef(name)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}