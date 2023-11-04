package its.model.expressions.literals

import its.model.DomainModel
import its.model.expressions.Operator
import its.model.expressions.types.EnumValue
import its.model.expressions.types.Types
import its.model.expressions.visitors.LiteralBehaviour
import kotlin.reflect.KClass

/**
 * Enum литерал
 * @param value Значение
 */
class EnumLiteral(value: EnumValue) : ValueLiteral<EnumValue>(value) {

    init {
        // Проверяем существование enum и наличие у него такого значения
        require(DomainModel.enumsDictionary.exist(value.ownerEnum)) { "Enum ${value.ownerEnum} не объявлен в словаре." }
        require(DomainModel.enumsDictionary.containsValue(value) == true) {
            "Enum ${value.ownerEnum} не содержит значения ${value.value}."
        }
    }

    override val resultDataType: KClass<EnumValue>
        get() = Types.Enum

    override fun clone(): Operator = EnumLiteral(value)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}