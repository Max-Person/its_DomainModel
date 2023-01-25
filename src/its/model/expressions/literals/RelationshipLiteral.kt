package its.model.expressions.literals

import its.model.DomainModel
import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType

/**
 * Relationship литерал
 * @param value Имя отношения
 */
class RelationshipLiteral(value: String) : Literal(value) {

    init {
        // Проверяем существование отношения
        require(DomainModel.relationshipsDictionary.exist(value)) { "Отношение $value не объявлено в словаре." }
    }

    override val resultDataType: DataType
        get() = DataType.Relationship

    override fun clone(): Operator = RelationshipLiteral(value)
}