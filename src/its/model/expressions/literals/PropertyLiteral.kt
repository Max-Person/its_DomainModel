package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.dictionaries.PropertiesDictionary
import its.model.util.DataType

/**
 * Property литерал
 * @param value Имя свойства
 */
class PropertyLiteral(value: String) : Literal(value) {

    init {
        // Проверяем существование свойства
        require(PropertiesDictionary.exist(value)) { "Свойство $value не объявлено в словаре." }
    }

    override val resultDataType: DataType
        get() = DataType.Property

    override fun clone(): Operator = PropertyLiteral(value)
}