package its.model.expressions.literals

import its.model.DomainModel
import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.expressions.visitors.OperatorBehaviour
import its.model.expressions.visitors.OperatorVisitor

/**
 * Enum литерал
 * @param value Значение
 * @param owner Имя enum, к которому относится данный элемент
 */
class EnumLiteral(value: String, val owner: String) : Literal(value) {

    init {
        // Проверяем существование enum и наличие у него такого значения
        require(DomainModel.enumsDictionary.exist(owner)) { "Enum $owner не объявлен в словаре." }
        require(DomainModel.enumsDictionary.containsValue(owner, value) == true) {
            "Enum $owner не содержит значения $value."
        }
    }

    override val resultDataType: DataType
        get() = DataType.Enum

    override fun clone(): Operator = EnumLiteral(value, owner)

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as EnumLiteral

        if (owner != other.owner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + owner.hashCode()
        return result
    }
}