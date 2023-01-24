package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.util.CompilationResult
import its.model.util.DataType

/**
 * Квантор общности
 */
class ForAllQuantifier(
    args: List<Operator>,
    private val varName: String
) : BaseOperator(args) {

    /**
     * Является ли оператор негативным (т.е. нужно ли отрицание при компиляции)
     */
    internal var isNegative = false

    override val argsDataTypes = listOf(listOf(DataType.Boolean, DataType.Boolean))


    override val resultDataType = DataType.Boolean

    override fun compile(): CompilationResult {
        // Компилируем через другие операторы
        val not = LogicalNot(listOf(arg(1)))
        val and = LogicalAnd(listOf(arg(0), not))

        val existence = ExistenceQuantifier(listOf(and), varName)

        val res = if (isNegative) {
            LogicalNot(listOf(existence))
        } else {
            existence
        }

        return res.semantic().compile()
    }

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return ForAllQuantifier(newArgs, varName)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return ForAllQuantifier(newArgs, varName)
    }
}