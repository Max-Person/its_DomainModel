package ru.compprehension.its.model.expressions.operators

import ru.compprehension.its.model.DomainModel
import ru.compprehension.its.model.expressions.Operator
import ru.compprehension.its.model.expressions.literals.*
import ru.compprehension.its.model.expressions.types.Types
import ru.compprehension.its.model.expressions.visitors.OperatorBehaviour

/**
 * Присваивание
 */
class AssignProperty(args: List<Operator>) : BaseOperator(args) {

    init {
        if (args.size == 3) {
            val arg2 = arg(2)

            val propertyName = (arg(1) as PropertyRef).name
            val newValueDataType = arg(2).resultDataType

            require(DomainModel.propertiesDictionary.isStatic(propertyName) == false) {
                "Статическому свойству $propertyName нельзя присваивать значения."
            }
            require(DomainModel.propertiesDictionary.dataType(propertyName) == newValueDataType) {
                "Тип данных $newValueDataType не соответствует типу ${
                    DomainModel.propertiesDictionary.dataType(
                        propertyName
                    )
                } свойства $propertyName."
            }
            require(arg2 is Literal || arg2 is GetPropertyValue) {
                "Нельзя присвоить свойству динамическое значение." // FIXME?: можно, но тогда не получится его контролировать
            }

            // FIXME?: проверять попадает ли в диапазон значение при arg2 is GetPropertyValue?
            when (arg2) {
                is IntegerLiteral -> {
                    require(DomainModel.propertiesDictionary.isValueInRange(propertyName, arg2.value) == true) {
                        "Значение ${arg2.value} вне диапазона значений свойства $propertyName."
                    }
                }

                is DoubleLiteral -> {
                    require(DomainModel.propertiesDictionary.isValueInRange(propertyName, arg2.value) == true) {
                        "Значение ${arg2.value} вне диапазона значений свойства $propertyName."
                    }
                }

                is EnumLiteral -> {
                    require(DomainModel.propertiesDictionary.enumName(propertyName)!! == arg2.value.ownerEnum) {
                        "Тип перечисления ${DomainModel.propertiesDictionary.enumName(propertyName)} свойства $propertyName не соответствует типу перечисления ${arg2.value.ownerEnum} значения."
                    }
                }

                is GetPropertyValue -> {
                    require(
                        DomainModel.propertiesDictionary.enumName(propertyName)!!
                                == DomainModel.propertiesDictionary.enumName((arg2.arg(1) as PropertyRef).name)!!
                    ) {
                        "Тип перечисления ${DomainModel.propertiesDictionary.enumName(propertyName)} свойства $propertyName не соответствует типу перечисления ${
                            DomainModel.propertiesDictionary.enumName(
                                (arg2.arg(1) as PropertyRef).name
                            )
                        } значения."
                    }
                }
            }
        }
    }

    override val argsDataTypes
        get() = listOf(
            listOf(Types.Object, PropertyRef::class, Types.Integer),
            listOf(Types.Object, PropertyRef::class, Types.Double),
            listOf(Types.Object, PropertyRef::class, Types.Boolean),
            listOf(Types.Object, PropertyRef::class, Types.String),
            listOf(Types.Object, PropertyRef::class, Types.Enum),
        )

    val objectExpr get() = arg(0)
    val propertyName get() = (arg(1) as PropertyRef).name
    val valueExpr get() = arg(2)

    override val resultDataType
        get() = Types.None

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return AssignProperty(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return AssignProperty(newArgs)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}