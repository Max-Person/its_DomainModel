package its.model.expressions.literals

import its.model.DomainModel
import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.LiteralBehaviour
import kotlin.reflect.KClass

/**
 * Relationship литерал
 * @param name Имя отношения
 */
class RelationshipRef(name: String) : ReferenceLiteral(name) {

    init {
        // Проверяем существование отношения
        require(DomainModel.relationshipsDictionary.exist(name)) { "Отношение $name не объявлено в словаре." }
    }

    override val resultDataType: KClass<String>
        get() = Types.String //FIXME

    override fun clone(): Operator = RelationshipRef(name)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}