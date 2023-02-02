package its.model.expressions.visitors

import its.model.expressions.literals.*

interface LiteralBehaviour<Info> {
    // -------------------- Для листьев дерева выражений ---------------------
    fun process(literal: BooleanLiteral): Info
    fun process(literal: ClassLiteral): Info
    fun process(literal: ComparisonResultLiteral): Info
    fun process(literal: DecisionTreeVarLiteral): Info
    fun process(literal: DoubleLiteral): Info
    fun process(literal: EnumLiteral): Info
    fun process(literal: IntegerLiteral): Info
    fun process(literal: ObjectLiteral): Info
    fun process(literal: PropertyLiteral): Info
    fun process(literal: RelationshipLiteral): Info
    fun process(literal: StringLiteral): Info
}