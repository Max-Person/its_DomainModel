package its.model.expressions.visitors

import its.model.expressions.literals.*

/**
 * Интерфейс, описывающий некоторое поведение, внедряемое в литералы дерева выражения (подклассы [Literal])
 * @param Info тип возвращаемого функциями поведения значения
 * @see Literal.use
 */
//FIXME? наверное не нужно с введением TypeBehaviour и переработкой системы типов
interface LiteralBehaviour<Info> {
    // -------------------- Для листьев дерева выражений ---------------------
    fun process(literal: BooleanLiteral): Info
    fun process(literal: ClassLiteral): Info

    //    fun process(literal: ComparisonResultLiteral): Info
    fun process(literal: DecisionTreeVarLiteral): Info
    fun process(literal: VariableLiteral): Info
    fun process(literal: DoubleLiteral): Info
    fun process(literal: EnumLiteral): Info
    fun process(literal: IntegerLiteral): Info
    fun process(literal: ObjectLiteral): Info
    fun process(literal: StringLiteral): Info
}