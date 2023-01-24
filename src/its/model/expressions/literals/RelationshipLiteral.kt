package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.util.CompilationResult
import its.model.dictionaries.RelationshipsDictionary
import its.model.util.DataType
import its.model.util.JenaUtil
import its.model.util.JenaUtil.POAS_PREF

/**
 * Relationship литерал
 * @param value Имя отношения
 */
class RelationshipLiteral(value: String) : Literal(value) {

    init {
        // Проверяем существование отношения
        require(RelationshipsDictionary.exist(value)) { "Отношение $value не объявлено в словаре." }
    }

    override val resultDataType: DataType
        get() = DataType.Relationship

    override fun compile(): CompilationResult =
        CompilationResult(value = JenaUtil.genLink(POAS_PREF, value))

    override fun clone(): Operator = RelationshipLiteral(value)
}