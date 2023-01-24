package its.model.expressions.literals

import its.model.dictionaries.EnumsDictionary
import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.util.DataType

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

    override fun clone(): Operator = EnumLiteral(value, owner)
}