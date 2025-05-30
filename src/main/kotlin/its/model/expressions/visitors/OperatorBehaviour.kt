package its.model.expressions.visitors

import its.model.expressions.Operator
import its.model.expressions.operators.*

/**
 * Интерфейс, описывающий некоторое поведение, внедряемое в узлы дерева выражения (подклассы [Operator])
 * @param Info тип возвращаемого функциями поведения значения
 * @see Operator.use
 */
interface OperatorBehaviour<Info> : LiteralBehaviour<Info> {
    // ------------------ Для узлов дерева выражений -------------------------
    fun process(op: AssignProperty): Info
    fun process(op: AssignDecisionTreeVar): Info
    fun process(op: Cast): Info
    fun process(op: CheckClass): Info
    fun process(op: CheckRelationship): Info
    fun process(op: GetRelationshipParamValue): Info
    fun process(op: Compare): Info
    fun process(op: CompareWithComparisonOperator): Info
    fun process(op: ExistenceQuantifier): Info
    fun process(op: ForAllQuantifier): Info
    fun process(op: GetByCondition): Info
    fun process(op: GetByRelationship): Info
    fun process(op: GetClass): Info
    fun process(op: GetExtreme): Info
    fun process(op: GetPropertyValue): Info
    fun process(op: LogicalAnd): Info
    fun process(op: LogicalNot): Info
    fun process(op: LogicalOr): Info
    fun process(op: AddRelationshipLink): Info
    fun process(op: Block): Info
    fun process(op: IfThen): Info
}