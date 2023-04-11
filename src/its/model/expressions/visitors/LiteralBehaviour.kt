package its.model.expressions.visitors

import its.model.expressions.literals.*

/**
 * Интерфейс, описывающий некоторое поведение, внедряемое в литералы дерева выражения (подклассы [Literal])
 * @param Info тип возвращаемого функциями поведения значения
 * @see Literal.use
 */
interface LiteralBehaviour<Info> {
    // -------------------- Для листьев дерева выражений ---------------------
    fun process(literal: BooleanLiteral): Info
    fun process(literal: ClassRef): Info
    fun process(literal: ComparisonResultLiteral): Info
    fun process(literal: DecisionTreeVar): Info
    fun process(literal: Variable): Info
    fun process(literal: DoubleLiteral): Info
    fun process(literal: EnumLiteral): Info
    fun process(literal: IntegerLiteral): Info
    fun process(literal: ObjectRef): Info
    fun process(literal: PropertyRef): Info
    fun process(literal: RelationshipRef): Info
    fun process(literal: StringLiteral): Info
}