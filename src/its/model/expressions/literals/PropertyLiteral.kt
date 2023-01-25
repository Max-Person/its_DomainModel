package its.model.expressions.literals

import its.model.DomainModel
import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType

/**
 * Property литерал
 * @param value Имя свойства
 */
class PropertyLiteral(value: String) : Literal(value) {

    init {
        // Проверяем существование свойства
        require(DomainModel.propertiesDictionary.exist(value)) { "Свойство $value не объявлено в словаре." }
    }

    override val resultDataType: DataType
        get() = DataType.Property

    override fun clone(): Operator = PropertyLiteral(value)
}