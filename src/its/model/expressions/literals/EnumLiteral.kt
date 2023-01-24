package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.util.CompilationResult
import its.model.dictionaries.EnumsDictionary
import its.model.util.DataType
import its.model.util.JenaUtil

/**
 * Enum литерал
 * @param value Значение
 * @param owner Имя enum, к которому относится данный элемент
 */
class EnumLiteral(value: String, val owner: String) : Literal(value) {

    init {
        // Проверяем существование enum и наличие у него такого значения
        require(EnumsDictionary.exist(owner)) { "Enum $owner не объявлен в словаре." }
        require(EnumsDictionary.containsValue(owner, value) == true) {
            "Enum $owner не содержит значения $value."
        }
    }

    override val resultDataType: DataType
        get() = DataType.Enum

    override fun compile(): CompilationResult =
        CompilationResult(value = JenaUtil.genLink(JenaUtil.POAS_PREF, value))

    override fun clone(): Operator = EnumLiteral(value, owner)
}