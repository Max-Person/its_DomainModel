package its.model.expressions.visitors

import its.model.expressions.Literal
import its.model.expressions.Variable
import its.model.expressions.literals.*
import its.model.expressions.operators.*

/**
 * Простая реализация интерфейса [OperatorVisitor] для случаев,
 * когда логика обработки разных типов узлов не так важна, как факт обхода выражения.
 * @see OperatorVisitor
 */
abstract class SimpleOperatorVisitor<Info> : OperatorVisitor<Info> {

    // -------------------- Для листьев дерева выражений ---------------------
    override fun process(literal: BooleanLiteral) : Info {return process(literal as Literal)}
    override fun process(literal: ClassLiteral) : Info {return process(literal as Literal)}
    override fun process(literal: ComparisonResultLiteral) : Info {return process(literal as Literal)}
    override fun process(literal: DecisionTreeVarLiteral) : Info {return process(literal as Literal)}
    override fun process(literal: DoubleLiteral) : Info {return process(literal as Literal)}
    override fun process(literal: EnumLiteral) : Info {return process(literal as Literal)}
    override fun process(literal: IntegerLiteral) : Info {return process(literal as Literal)}
    override fun process(literal: ObjectLiteral) : Info {return process(literal as Literal)}
    override fun process(literal: PropertyLiteral) : Info {return process(literal as Literal)}
    override fun process(literal: RelationshipLiteral) : Info {return process(literal as Literal)}
    override fun process(literal: StringLiteral) : Info {return process(literal as Literal)}

    abstract override fun process(variable: Variable) : Info
    abstract fun process(literal : Literal) : Info

    // ------------------ Для узлов дерева выражений -------------------------
    // ------------------ До обработки дочерних узлов ------------------------
    override fun process(op: Assign) : Info {return process(op as BaseOperator)}
    override fun process(op: CheckClass) : Info {return process(op as BaseOperator)}
    override fun process(op: CheckPropertyValue) : Info {return process(op as BaseOperator)}
    override fun process(op: CheckRelationship) : Info {return process(op as BaseOperator)}
    override fun process(op: Compare) : Info {return process(op as BaseOperator)}
    override fun process(op: CompareWithComparisonOperator) : Info {return process(op as BaseOperator)}
    override fun process(op: ExistenceQuantifier) : Info {return process(op as BaseOperator)}
    override fun process(op: ForAllQuantifier) : Info {return process(op as BaseOperator)}
    override fun process(op: GetByCondition) : Info {return process(op as BaseOperator)}
    override fun process(op: GetByRelationship) : Info {return process(op as BaseOperator)}
    override fun process(op: GetClass) : Info {return process(op as BaseOperator)}
    override fun process(op: GetExtreme) : Info {return process(op as BaseOperator)}
    override fun process(op: GetPropertyValue) : Info {return process(op as BaseOperator)}
    //override fun process(op: IsReachable) : Info {return process(op as BaseOperator)}
    override fun process(op: LogicalAnd) : Info {return process(op as BaseOperator)}
    override fun process(op: LogicalNot) : Info {return process(op as BaseOperator)}
    override fun process(op: LogicalOr) : Info {return process(op as BaseOperator)}

    abstract fun process(op: BaseOperator) : Info

    // ---------------------- Для узлов дерева выражений ---------------------------
    // --------------------- После обработки дочерних узлов ------------------------
    override fun process(op: Assign, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: CheckClass, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: CheckPropertyValue, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: CheckRelationship, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: Compare, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: CompareWithComparisonOperator, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: ExistenceQuantifier, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: ForAllQuantifier, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: GetByCondition, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: GetByRelationship, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: GetClass, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: GetExtreme, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: GetPropertyValue, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    //override fun process(op: IsReachable,            currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: LogicalAnd, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: LogicalNot, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}
    override fun process(op: LogicalOr, currentInfo : Info, argInfo: List<Info>) : Info {return process(op as BaseOperator, currentInfo, argInfo)}

    abstract fun process(op: BaseOperator, currentInfo : Info, argInfo: List<Info>) : Info
}