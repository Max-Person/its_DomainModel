package its.model.expressions.operators

import its.model.DomainModel
import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.literals.DoubleLiteral
import its.model.expressions.literals.EnumLiteral
import its.model.expressions.literals.IntegerLiteral
import its.model.expressions.literals.PropertyLiteral
import its.model.expressions.types.DataType

/**
 * Присваивание
 */
class Assign(args: List<Operator>) : BaseOperator(args) {

    init {
        if (args.size == 3) {
            val arg2 = arg(2)

            val propertyName = (arg(1) as PropertyLiteral).value
            val newValueDataType = arg(2).resultDataType!!

            require(DomainModel.propertiesDictionary.isStatic(propertyName) == false) {
                "Свойство $propertyName не должно быть статическим."
            }
            require(DomainModel.propertiesDictionary.dataType(propertyName) == newValueDataType) {
                "Тип данных $newValueDataType не соответствует типу ${DomainModel.propertiesDictionary.dataType(propertyName)} свойства $propertyName."
            }
            require(arg2 is Literal || arg2 is GetPropertyValue) {
                "Нельзя присвоить свойству динамическое значение." // FIXME?: можно, но тогда не получится его контролировать
            }

            // FIXME?: проверять попадает ли в диапазон значение при arg2 is GetPropertyValue?
            when (arg2) {
                is IntegerLiteral -> {
                    require(DomainModel.propertiesDictionary.isValueInRange(propertyName, arg2.value.toInt()) == true) {
                        "Значение ${arg2.value.toInt()} вне диапазона значений свойства $propertyName."
                    }
                }

                is DoubleLiteral -> {
                    require(DomainModel.propertiesDictionary.isValueInRange(propertyName, arg2.value.toDouble()) == true) {
                        "Значение ${arg2.value.toDouble()} вне диапазона значений свойства $propertyName."
                    }
                }

                is EnumLiteral -> {
                    require(DomainModel.propertiesDictionary.enumName(propertyName)!! == arg2.owner) {
                        "Тип перечисления ${DomainModel.propertiesDictionary.enumName(propertyName)} свойства $propertyName не соответствует типу перечисления ${arg2.owner} значения."
                    }
                }

                is GetPropertyValue -> {
                    require(
                        DomainModel.propertiesDictionary.enumName(propertyName)!!
                                == DomainModel.propertiesDictionary.enumName((arg2.arg(1) as PropertyLiteral).value)!!
                    ) {
                        "Тип перечисления ${DomainModel.propertiesDictionary.enumName(propertyName)} свойства $propertyName не соответствует типу перечисления ${
                            DomainModel.propertiesDictionary.enumName(
                                (arg2.arg(1) as PropertyLiteral).value
                            )
                        } значения."
                    }
                }
            }
        }
    }

    override val argsDataTypes
        get() = listOf(
            listOf(DataType.Object, DataType.Property, DataType.Integer),
            listOf(DataType.Object, DataType.Property, DataType.Double),
            listOf(DataType.Object, DataType.Property, DataType.Boolean),
            listOf(DataType.Object, DataType.Property, DataType.String),
            listOf(DataType.Object, DataType.Property, DataType.Enum),
            listOf(DataType.DecisionTreeVar, DataType.Object)
        )

    override val resultDataType
        get() = null

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return Assign(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return Assign(newArgs)
    }
}