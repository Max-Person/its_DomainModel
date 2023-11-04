package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.literals.PropertyRef
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Оператор проверки значения свойства объекта
 */
class CheckPropertyValue(args: List<Operator>) : BaseOperator(args) {

    /**
     * Является ли оператор негативным (т.е. нужно ли отрицание при компиляции)
     */
    internal var isNegative = false

    override val argsDataTypes
        get() = listOf(
            listOf(Types.Object, PropertyRef::class, Types.Integer),
            listOf(Types.Object, PropertyRef::class, Types.Double),
            listOf(Types.Object, PropertyRef::class, Types.Boolean),
            listOf(Types.Object, PropertyRef::class, Types.String),
            listOf(Types.Object, PropertyRef::class, Types.Enum)
        )

    val objectExpr get() = arg(0)
    val propertyName get() = (arg(1) as PropertyRef).name
    val valueExpr get() = arg(2)

    override val resultDataType get() = Types.Boolean

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return CheckPropertyValue(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return CheckPropertyValue(newArgs)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}