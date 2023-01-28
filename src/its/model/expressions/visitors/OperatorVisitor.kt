package its.model.expressions.visitors

import its.model.expressions.Variable
import its.model.expressions.Operator
import its.model.expressions.literals.*
import its.model.expressions.operators.*

/**
 * Интерфейс, описывающий некоторое поведение, применяемое при обходе выражений (подклассы [Operator])
 *
 * Отличие данного интерфейса от [OperatorBehaviour] в том,
 * что [OperatorVisitor] реализовывается с целью полного обхода дерева решений
 * (Функция [Operator.accept] реализует логику обхода дерева).
 * Таким образом, данный класс стоит считать частным случаем соответсвующего -Behaviour класса.
 * @see Operator.accept
 */
interface OperatorVisitor<Info> {

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
    // ------------------ До обработки дочерних узлов ------------------------
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

    // ---------------------- Для узлов дерева выражений ---------------------------
    // --------------------- После обработки дочерних узлов ------------------------
    fun process(op: Assign,                 currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: CheckClass,             currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: CheckPropertyValue,     currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: CheckRelationship,      currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: Compare,                currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: CompareWithComparisonOperator,  currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: ExistenceQuantifier,            currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: ForAllQuantifier,               currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: GetByCondition,                 currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: GetByRelationship,              currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: GetClass,               currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: GetExtreme,             currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: GetPropertyValue,       currentInfo : Info, argInfo: List<Info>) : Info
    //fun process(op: IsReachable,            currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: LogicalAnd,             currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: LogicalNot,             currentInfo : Info, argInfo: List<Info>) : Info
    fun process(op: LogicalOr,              currentInfo : Info, argInfo: List<Info>) : Info
}