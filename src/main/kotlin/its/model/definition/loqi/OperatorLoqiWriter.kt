package its.model.definition.loqi

import its.model.definition.EnumValueRef
import its.model.definition.loqi.LoqiStringUtils.insertEscapes
import its.model.definition.loqi.LoqiStringUtils.toLoqiName
import its.model.expressions.Operator
import its.model.expressions.literals.*
import its.model.expressions.operators.*
import its.model.expressions.utils.NamedParamsValuesExprList
import its.model.expressions.utils.OrderedParamsValuesExprList
import its.model.expressions.utils.ParamsValuesExprList
import its.model.expressions.visitors.OperatorBehaviour
import java.io.StringWriter
import java.io.Writer

/**
 * Запись выражений [Operator] в LOQI формат
 * @see OperatorLoqiBuilder
 */
class OperatorLoqiWriter private constructor(
    writer: Writer,
) : OperatorBehaviour<Unit> {
    private val iWriter = IndentPrintWriter(writer)
    private fun indent() = iWriter.indent()
    private fun unindent() = iWriter.unindent()
    private fun write(s: Any) = iWriter.print(s)
    private fun writeln(s: Any) = iWriter.println(s)
    private fun newLine() = iWriter.println()
    private fun skipLines(n: Int = 2) = repeat(n) { newLine() }

    companion object {

        /**
         * Записать выражение [operator] в [writer]
         * @see getWrittenExpression
         */
        @JvmStatic
        fun writeExpression(operator: Operator, writer: Writer) {
            operator.use(OperatorLoqiWriter(writer))
        }

        /**
         * Получить строковую запись выражения [operator]
         * @see writeExpression
         */
        @JvmStatic
        fun getWrittenExpression(operator: Operator): String {
            val writer = StringWriter()
            writeExpression(operator, writer)
            return writer.toString()
        }

        private fun asString(writeBlock: OperatorLoqiWriter.() -> Unit): String {
            val writer = StringWriter()
            with(OperatorLoqiWriter(writer)) {
                writeBlock()
            }
            return writer.toString()
        }
    }

    private fun writeValueLiteral(valueLiteral: ValueLiteral<*, *>) {
        val value = valueLiteral.value
        val string = when (value) {
            is String -> "\"${value.insertEscapes()}\""
            is EnumValueRef -> "${value.enumName.toLoqiName()}:${value.valueName.toLoqiName()}"
            else -> value.toString()
        }
        write(string)
    }

    override fun process(literal: BooleanLiteral) = writeValueLiteral(literal)
    override fun process(literal: DoubleLiteral) = writeValueLiteral(literal)
    override fun process(literal: EnumLiteral) = writeValueLiteral(literal)
    override fun process(literal: IntegerLiteral) = writeValueLiteral(literal)
    override fun process(literal: StringLiteral) = writeValueLiteral(literal)


    override fun process(literal: DecisionTreeVarLiteral) {
        write(literal.name.toLoqiName())
    }

    override fun process(literal: VariableLiteral) {
        write("$${literal.name.toLoqiName()}")
    }

    override fun process(literal: ClassLiteral) {
        write("class:${literal.name.toLoqiName()}")
    }

    override fun process(literal: ObjectLiteral) {
        write("obj:${literal.name.toLoqiName()}")
    }

    private fun writeParams(paramsValuesExprList: ParamsValuesExprList) {
        if (paramsValuesExprList.getExprList().isEmpty()) {
            return
        }
        val paramsStrings = when (paramsValuesExprList) {
            is OrderedParamsValuesExprList -> {
                paramsValuesExprList.values.map { asString { it.write() } }
            }

            is NamedParamsValuesExprList -> {
                paramsValuesExprList.valuesMap.map { (paramName, valueExpr) ->
                    asString {
                        writeAndContinue("${paramName.toLoqiName()} = ", asString { valueExpr.write() })
                    }
                }
            }
        }
        writeMultipleEnclosedStrings("<", paramsStrings, ",", ">")
    }

    override fun process(op: GetByRelationship) {
        writeAndContinue(
            asString { op.subjectExpr.writeLeft(op) },
            "->"
                + op.relationshipName.toLoqiName()
                + asString { writeParams(op.paramsValues) }
        )
    }

    override fun process(op: GetClass) {
        writeAndContinue(asString { op.objectExpr.writeLeft(op) }, ".class()")
    }

    override fun process(op: GetPropertyValue) {
        writeAndContinue(
            asString { op.objectExpr.writeLeft(op) },
            "."
                + op.propertyName.toLoqiName()
                + asString { writeParams(op.paramsValues) })
    }

    override fun process(op: CheckRelationship) {
        writeAndContinue(
            asString { op.subjectExpr.writeLeft(op) },
            "->"
                + op.relationshipName.toLoqiName()
                + asString { writeMultipleEnclosed("(", op.objectExprs, ",", ")") }
                + asString { writeParams(op.paramsValues) },
        )
    }

    override fun process(op: GetRelationshipParamValue) {
        writeAndContinue(asString { op.subjectExpr.writeLeft(op) },
            "->"
                + op.relationshipName.toLoqiName()
                + asString { writeMultipleEnclosed("(", op.objectExprs, ",", ")") }
                + asString { writeParams(op.paramsValues) },
            ".${op.paramName.toLoqiName()}"
        )
    }

    private fun writeBinaryOperator(op: Operator, left: Operator, opStr: String, right: Operator) {
        left.writeLeft(op)
        write(" ${opStr.trim()} ")
        right.writeRight(op)
    }

    override fun process(op: Cast) {
        writeBinaryOperator(op, op.objectExpr, "as", op.classExpr)
    }

    override fun process(op: CheckClass) {
        writeBinaryOperator(op, op.objectExpr, "is", op.classExpr)
    }

    override fun process(op: Compare) {
        writeAndContinue(asString { op.firstExpr.writeLeft(op) },
            ".compare" + asString { op.secondExpr.writeEnclosed() })
    }

    override fun process(op: CompareWithComparisonOperator) {
        val opStr = when (op.operator) {
            CompareWithComparisonOperator.ComparisonOperator.Less -> "<"
            CompareWithComparisonOperator.ComparisonOperator.Greater -> ">"
            CompareWithComparisonOperator.ComparisonOperator.Equal -> "=="
            CompareWithComparisonOperator.ComparisonOperator.LessEqual -> "<="
            CompareWithComparisonOperator.ComparisonOperator.GreaterEqual -> ">="
            CompareWithComparisonOperator.ComparisonOperator.NotEqual -> "!="
        }
        writeBinaryOperator(op, op.firstExpr, opStr, op.secondExpr)
    }

    override fun process(op: LogicalNot) {
        write("not ")
        op.operandExpr.writeRight(op)
    }

    override fun process(op: LogicalAnd) {
        writeBinaryOperator(op, op.firstExpr, "and", op.secondExpr)
    }

    override fun process(op: LogicalOr) {
        writeBinaryOperator(op, op.firstExpr, "or", op.secondExpr)
    }

    override fun process(op: AssignProperty) {
        val left = GetPropertyValue(
            op.objectExpr,
            op.propertyName,
            op.paramsValues,
        )
        writeBinaryOperator(op, left, "=", op.valueExpr)
    }

    override fun process(op: AssignDecisionTreeVar) {
        val left = DecisionTreeVarLiteral(op.variableName)
        writeBinaryOperator(op, left, "=", op.valueExpr)
    }

    override fun process(op: AddRelationshipLink) {
        writeAndContinue(
            asString { op.subjectExpr.writeLeft(op) },
            "+=>"
                + op.relationshipName.toLoqiName()
                + asString { writeParams(op.paramsValues) }
                + asString { writeMultipleEnclosed("(", op.objectExprs, ",", ")") },
        )
    }

    override fun process(op: Block) {
        writeln("{")
        indent()
        write(op.nestedExprs.joinToString(";\n") { asString { it.write() } })
        newLine()
        unindent()
        write("}")
    }

    override fun process(op: IfThen) {
        if (op.elseExpr != null) {
            writeAndContinue(
                asString { op.conditionExpr.writeLeft(op) },
                "? " + asString { op.thenExpr.write() },
                ": " + asString { op.elseExpr.writeRight(op) }
            )
        } else {
            write("if ${asString { op.conditionExpr.writeEnclosed() }}")
            if (op.thenExpr is Block) {
                write(" ")
                op.thenExpr.write()
            } else {
                newLine()
                indent()
                op.thenExpr.write()
                unindent()
                newLine()
            }
        }
    }

    override fun process(op: GetByCondition) {
        write("find ${op.variable.className.toLoqiName()} ${op.variable.varName.toLoqiName()} ")
        writeEnclosed("{", asString { op.conditionExpr.write() }, "}")
    }

    override fun process(op: GetExtreme) {
        write("findExtreme ${op.extremeVarName.toLoqiName()} ")
        writeEnclosed("[", asString { op.extremeConditionExpr.write() }, "] ")
        write("among ${op.className.toLoqiName()} ${op.varName.toLoqiName()} ")
        writeEnclosed("{", asString { op.conditionExpr.write() }, "}")
    }

    override fun process(op: ExistenceQuantifier) {
        write("forAny ${op.variable.className.toLoqiName()} ${op.variable.varName.toLoqiName()} ")
        if (op.selectorExpr != null) {
            writeEnclosed("[", asString { op.selectorExpr.write() }, "] ")
        }
        writeEnclosed("{", asString { op.conditionExpr.write() }, "}")
    }

    override fun process(op: ForAllQuantifier) {
        write("forAll ${op.variable.className.toLoqiName()} ${op.variable.varName.toLoqiName()} ")
        if (op.selectorExpr != null) {
            writeEnclosed("[", asString { op.selectorExpr.write() }, "] ")
        }
        writeEnclosed("{", asString { op.conditionExpr.write() }, "}")
    }

    //------------------------------------------

    private fun String.isComplex(): Boolean {
        return this.contains("\n") || this.length > 40
    }

    private fun Operator.write() {
        return this.use(this@OperatorLoqiWriter)
    }

    private fun writeAndContinue(first: String, vararg continuations: String) {
        val simpleString = first + continuations.joinToString("")
        if (simpleString.isComplex()) {
            writeln(first.trim())
            indent()
            write(continuations.joinToString("\n") { it.trim() })
            unindent()
        } else {
            write(simpleString)
        }
    }

    private fun writeMultipleEnclosed(open: String, nested: Collection<Operator>, separator: String, close: String) {
        writeMultipleEnclosedStrings(open, nested.map { asString { it.write() } }, separator, close)
    }

    private fun writeMultipleEnclosedStrings(
        open: String,
        nested: Collection<String>,
        separator: String,
        close: String,
    ) {
        val simpleString = nested.joinToString(separator + " ")
        if (simpleString.isComplex()) {
            writeEnclosed(open, nested.joinToString(separator + "\n"), close)
        } else {
            writeEnclosed(open, simpleString, close)
        }
    }

    private fun writeEnclosed(open: String, nested: String, close: String) {
        if (nested.isComplex()) {
            writeln(open)
            indent()
            write(nested)
            newLine()
            unindent()
            write(close)
        } else {
            write("$open$nested$close")
        }
    }

    private fun Operator.writeEnclosed() {
        writeEnclosed("(", asString { this@writeEnclosed.write() }, ")")
    }

    private fun Operator.writeLeft(parent: Operator) {
        writeEnclosed(parent, Associativity.RIGHT)
    }

    private fun Operator.writeRight(parent: Operator) {
        writeEnclosed(parent, Associativity.LEFT)
    }

    private fun Operator.writeEnclosed(parent: Operator, associativityToEnclose: Associativity) {
        val thisPrecedence = this.getPrecedence().level
        val parentPrecedence = parent.getPrecedence().level
        if (thisPrecedence < parentPrecedence || (thisPrecedence == parentPrecedence && this.getAssociativity() == associativityToEnclose)) {
            this.writeEnclosed()
        } else {
            this.write()
        }
    }

    private enum class Associativity {
        LEFT, RIGHT, NON_ASSOC,
        ;
    }

    private fun Operator.getAssociativity(): Associativity {
        val precedence = this.getPrecedence()
        return when (precedence) {
            Precedence.NOT_APPLICABLE -> Associativity.NON_ASSOC
            Precedence.TERNARY_CONDITIONAL, Precedence.ASSIGNMENT, Precedence.IF_CONDITIONAL -> Associativity.RIGHT
            else -> Associativity.LEFT
        }
    }

    private enum class Precedence {
        //От наибольшего
        LITERAL,
        GETTER,
        RELATIONAL,
        NOT,
        MAGNITUDE_COMPARISON,
        EQUALITY,
        CAST,
        AND,
        OR,
        ADD_RELATIONSHIP,
        TERNARY_CONDITIONAL,
        ASSIGNMENT,
        IF_CONDITIONAL,
        NOT_APPLICABLE, //К наименьшему
        ;

        val level = -this.ordinal
    }

    private fun Operator.getPrecedence(): Precedence {
        return this.use(OperatorPrecedenceGetter())
    }

    private class OperatorPrecedenceGetter : OperatorBehaviour<Precedence> {
        override fun process(literal: BooleanLiteral) = Precedence.LITERAL
        override fun process(literal: ClassLiteral) = Precedence.LITERAL
        override fun process(literal: DecisionTreeVarLiteral) = Precedence.LITERAL
        override fun process(literal: VariableLiteral) = Precedence.LITERAL
        override fun process(literal: DoubleLiteral) = Precedence.LITERAL
        override fun process(literal: EnumLiteral) = Precedence.LITERAL
        override fun process(literal: IntegerLiteral) = Precedence.LITERAL
        override fun process(literal: ObjectLiteral) = Precedence.LITERAL
        override fun process(literal: StringLiteral) = Precedence.LITERAL

        override fun process(op: GetPropertyValue) = Precedence.GETTER
        override fun process(op: GetByRelationship) = Precedence.GETTER
        override fun process(op: GetClass) = Precedence.GETTER

        override fun process(op: CheckRelationship) = Precedence.RELATIONAL
        override fun process(op: GetRelationshipParamValue) = Precedence.RELATIONAL

        override fun process(op: LogicalNot) = Precedence.NOT

        override fun process(op: CheckClass) = Precedence.MAGNITUDE_COMPARISON
        override fun process(op: Compare) = Precedence.MAGNITUDE_COMPARISON

        override fun process(op: CompareWithComparisonOperator): Precedence {
            return if (op.operator.isEquality) Precedence.EQUALITY
            else Precedence.MAGNITUDE_COMPARISON
        }

        override fun process(op: Cast) = Precedence.CAST

        override fun process(op: LogicalAnd) = Precedence.AND

        override fun process(op: LogicalOr) = Precedence.OR

        override fun process(op: AddRelationshipLink) = Precedence.ADD_RELATIONSHIP

        override fun process(op: IfThen): Precedence {
            return if (op.elseExpr != null) Precedence.TERNARY_CONDITIONAL
            else Precedence.IF_CONDITIONAL
        }

        override fun process(op: AssignProperty) = Precedence.ASSIGNMENT
        override fun process(op: AssignDecisionTreeVar) = Precedence.ASSIGNMENT

        override fun process(op: ExistenceQuantifier) = Precedence.NOT_APPLICABLE
        override fun process(op: ForAllQuantifier) = Precedence.NOT_APPLICABLE
        override fun process(op: GetByCondition) = Precedence.NOT_APPLICABLE
        override fun process(op: GetExtreme) = Precedence.NOT_APPLICABLE
        override fun process(op: Block) = Precedence.NOT_APPLICABLE
    }

}