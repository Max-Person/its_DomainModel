package its.model.expressions.literals

import its.model.DomainModel
import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.LiteralBehaviour

/**
 * Property литерал
 * @param name Имя свойства
 */
class PropertyRef(name: String) : ReferenceLiteral(name) {

    init {
        // Проверяем существование свойства
        require(DomainModel.propertiesDictionary.exist(name)) { "Свойство $name не объявлено в словаре." }
    }

    override val resultDataType
        get() = Types.String //FIXME

    override fun clone(): Operator = PropertyRef(name)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}