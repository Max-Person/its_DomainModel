package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.DataType

/**
 * Выбор экстремального объекта
 */
class GetExtreme(
    args: List<Operator>,
    val varName: String,
    val extremeVarName: String
): BaseOperator(args) {

    override val argsDataTypes = listOf(listOf(DataType.Boolean, DataType.Boolean))


    override val resultDataType = DataType.Object


    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return GetExtreme(newArgs, varName, extremeVarName)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return GetExtreme(newArgs, varName, extremeVarName)
    }
}