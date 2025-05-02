package its.model.nodes.xml

import its.model.TypedVariable
import its.model.build.xml.XMLWriter
import its.model.expressions.Operator
import its.model.expressions.xml.ExpressionXMLWriter
import its.model.nodes.*
import its.model.nodes.visitors.DecisionTreeBehaviour
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.Writer

/**
 * Запись дерева [DecisionTree] в XML.
 * @see DecisionTreeXMLBuilder
 */
class DecisionTreeXMLWriter(document: Document) : XMLWriter(document), DecisionTreeBehaviour<Element> {


    companion object {

        /**
         * Записать переданное дерево [decisionTree] в [writer] в формате xml
         */
        @JvmStatic
        fun writeDecisionTreeToXml(decisionTree: DecisionTree, writer: Writer) {
            writeToXml(writer) { document -> DecisionTreeXMLWriter(document).createTreeElement(decisionTree) }
        }

        /**
         * Преобразовать переданное дерево [decisionTree] в xml-строку
         */
        @JvmStatic
        fun decisionTreeToXmlString(decisionTree: DecisionTree) : String {
            return writeToXmlString { document -> DecisionTreeXMLWriter(document).createTreeElement(decisionTree) }
        }

        private const val OUTCOME_TAG = "Outcome"
        private const val EXPR_TAG = "Expression"
        private const val DECISION_TREE_VAR_DECL_TAG = "DecisionTreeVarDecl"
        private const val ADDITIONAL_DECISION_TREE_VAR_DECL_TAG = "AdditionalVarDecl"
        private const val THOUGHT_BRANCH_TAG = "ThoughtBranch"

        private const val VALUE_ATTR = "value"
        private const val NAME_ATTR = "name"
        private const val TYPE_ATTR = "type"
        private const val LOGICAL_OP_ATTR = "operator"

        private const val ADDITIONAL_INFO_PREFIX = "_"
    }

    private fun Element.withExpr(tagName: String, expr: Operator) : Element {
        return this.withChild(
            newElement(tagName)
                .withChild(ExpressionXMLWriter.expressionToElement(expr, document))
        )
    }

    private fun Element.withExpr(expr: Operator) : Element {
        return this.withExpr(EXPR_TAG, expr)
    }

    private fun Element.withMetadataOf(treeElement: DecisionTreeElement): Element {
        return this.apply {
            treeElement.metadata.entries
                .sortedBy { it.locCode }
                .forEach{(locCode, propertyName, propertyValue) ->
                    val attributePrefix = if(locCode != null) ADDITIONAL_INFO_PREFIX + locCode else ""
                    withAttribute(attributePrefix + ADDITIONAL_INFO_PREFIX + propertyName, propertyValue.toString())
                }
        }
    }

    private fun DecisionTreeNode.createElement(): Element {
        return this.use(this@DecisionTreeXMLWriter)
    }

    private fun Element.withOutcomes(outcomes: Outcomes<*>) : Element {
        return this.apply {
            outcomes.forEach{outcome ->
                withChild(
                    newElement("Outcome")
                        .withAttribute(VALUE_ATTR, outcome.key.toString())
                        .withChild(outcome.node.createElement())
                        .withMetadataOf(outcome)
                )
            }
        }
    }

    override fun process(node: BranchResultNode): Element {
        return newElement("BranchResultNode")
            .withAttribute(VALUE_ATTR, node.value.toString())
            .apply { if(node.actionExpr != null) withExpr(node.actionExpr) }
            .withMetadataOf(node)
    }

    override fun process(branch: ThoughtBranch): Element {
        return newElement(THOUGHT_BRANCH_TAG)
            .withChild(branch.start.createElement())
            .withMetadataOf(branch)
    }

    private fun Element.withThoughtBranch(branch: ThoughtBranch): Element {
        return this.withChild(process(branch))
    }

    private fun createTreeElement(decisionTree: DecisionTree): Element {
        return newElement("DecisionTree")
            .withChild(
                newElement("InputVariables")
                    .apply { decisionTree.variables.forEach { withDecisionTreeVarDecl(it) } }
                    .withAdditionalVarAssignments(decisionTree.implicitVariables)
            )
            .withThoughtBranch(decisionTree.mainBranch)
    }

    override fun process(node: QuestionNode): Element {
        return newElement("QuestionNode")
            .apply { if(node.trivialityExpr != null) withExpr("Triviality", node.trivialityExpr) }
            .apply { if(node.isSwitch) withAttribute("isSwitch", true.toString())}
            .withExpr(node.expr)
            .withOutcomes(node.outcomes)
            .withMetadataOf(node)
    }

    override fun processTupleQuestionNode(node: TupleQuestionNode): Element {
        return newElement("TupleQuestionNode")
            .apply { node.parts.forEach { part -> withChild(
                newElement("Part")
                    .withExpr(part.expr)
                    .apply { part.possibleOutcomes.forEach { outcome -> withChild(
                        newElement(OUTCOME_TAG)
                            .withAttribute(VALUE_ATTR, outcome.value.toString())
                            .withMetadataOf(outcome)
                    ) } }
                    .withMetadataOf(part)
            ) } }
            .withOutcomes(node.outcomes)
            .withMetadataOf(node)
    }

    private fun Element.withAdditionalVarAssignments(assignments: List<DecisionTreeVarAssignment>) : Element {
        assignments.forEach { assignment -> this.withChild(
            newElement(ADDITIONAL_DECISION_TREE_VAR_DECL_TAG)
                .withAttribute(NAME_ATTR, assignment.variable.varName)
                .withAttribute(TYPE_ATTR, assignment.variable.className)
                .withExpr(assignment.valueExpr)
        ) }
        return this
    }

    private fun Element.withDecisionTreeVarDecl(typedVariable: TypedVariable): Element {
        return this.withChild(
            newElement(DECISION_TREE_VAR_DECL_TAG)
                .withAttribute(NAME_ATTR, typedVariable.varName)
                .withAttribute(TYPE_ATTR, typedVariable.className)
        )
    }

    private fun Element.withFindErrorCategories(errorCategories: List<FindErrorCategory>, parentTypeName: String): Element {
        errorCategories.forEach { category -> this.withChild(
            newElement("FindError")
                .withAttribute("priority", category.priority.toString())
                .apply {
                    if(category.checkedVariable.className != parentTypeName)
                        withAttribute(TYPE_ATTR, category.checkedVariable.className)
                }
                .apply {
                    if(category.checkedVariable.varName != FindErrorCategory.CHECKED_OBJ)
                        withAttribute("checkedVar", category.checkedVariable.varName)
                }
                .withExpr(category.selectorExpr)
                .withMetadataOf(category)
        ) }
        return this
    }

    override fun process(node: FindActionNode): Element {
        return newElement("FindActionNode")
            .withDecisionTreeVarDecl(node.varAssignment.variable)
            .withExpr(node.varAssignment.valueExpr)
            .withFindErrorCategories(node.errorCategories, node.varAssignment.variable.className)
            .withAdditionalVarAssignments(node.secondaryAssignments)
            .withOutcomes(node.outcomes)
            .withMetadataOf(node)
    }

    override fun process(node: CycleAggregationNode): Element {
        return newElement("CycleAggregationNode")
            .withAttribute(LOGICAL_OP_ATTR, node.aggregationMethod.toString())
            .withDecisionTreeVarDecl(node.variable)
            .withExpr("SelectorExpression", node.selectorExpr)
            .withFindErrorCategories(node.errorCategories, node.variable.className)
            .withThoughtBranch(node.thoughtBranch)
            .withOutcomes(node.outcomes)
            .withMetadataOf(node)
    }

    override fun process(node: BranchAggregationNode): Element {
        return newElement("BranchAggregationNode")
            .withAttribute(LOGICAL_OP_ATTR, node.aggregationMethod.toString())
            .apply { node.thoughtBranches.forEach { withThoughtBranch(it) } }
            .withOutcomes(node.outcomes)
            .withMetadataOf(node)
    }

    override fun process(node: WhileCycleNode): Element {
        return newElement("WhileCycleNode")
            .withExpr("SelectorExpression", node.conditionExpr)
            .withThoughtBranch(node.thoughtBranch)
            .withOutcomes(node.outcomes)
            .withMetadataOf(node)
    }
}