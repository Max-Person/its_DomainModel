package its.model.expressions.visitors

import its.model.expressions.Variable
import its.model.expressions.Operator
import its.model.expressions.literals.*
import its.model.expressions.operators.*

/**
 * Интерфейс, описывающий некоторое поведение, внедряемое в узлы дерева выражения (подклассы [Operator])
 *
 * Отличие данного интерфейса от [OperatorBehaviour] в том,
 * что в общем случае [OperatorBehaviour] не подразумевает полного обхода дерева выражения.
 * Таким образом, данный класс стоит считать более общим случаем соответсвующего -Visitor класса.
 * @see Operator.use
 */
interface OperatorBehaviour<Info> {
    // -------------------- Для листьев дерева выражений ---------------------
    fun process(literal: BooleanLiteral) : Info
    fun process(literal: ClassLiteral) : Info
    fun process(literal: ComparisonResultLiteral) : Info
    fun process(literal: DecisionTreeVarLiteral) : Info
    fun process(literal: DoubleLiteral) : Info
    fun process(literal: EnumLiteral) : Info
    fun process(literal: IntegerLiteral) : Info
    fun process(literal: ObjectLiteral) : Info
    fun process(literal: PropertyLiteral) : Info
    fun process(literal: RelationshipLiteral) : Info
    fun process(literal: StringLiteral) : Info
    fun process(variable: Variable) : Info

    // ------------------ Для узлов дерева выражений -------------------------
    fun process(op: Assign) : Info
    fun process(op: CheckClass) : Info
    fun process(op: CheckPropertyValue) : Info
    fun process(op: CheckRelationship) : Info
    fun process(op: Compare) : Info
    fun process(op: CompareWithComparisonOperator) : Info
    fun process(op: ExistenceQuantifier) : Info
    fun process(op: ForAllQuantifier) : Info
    fun process(op: GetByCondition) : Info
    fun process(op: GetByRelationship) : Info
    fun process(op: GetClass) : Info
    fun process(op: GetExtreme) : Info
    fun process(op: GetPropertyValue) : Info
    //fun process(op: IsReachable) : Info
    fun process(op: LogicalAnd) : Info
    fun process(op: LogicalNot) : Info
    fun process(op: LogicalOr) : Info
}