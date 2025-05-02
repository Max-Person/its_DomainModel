package its.model.nodes.xml

import its.model.TypedVariable
import its.model.ValueTuple
import its.model.build.xml.ElementBuildContext
import its.model.build.xml.XMLBuildException
import its.model.build.xml.XMLBuilder
import its.model.definition.build.DomainBuilderUtils
import its.model.definition.types.Clazz
import its.model.definition.types.EnumValue
import its.model.definition.types.Obj
import its.model.expressions.Operator
import its.model.expressions.xml.ExpressionXMLBuilder
import its.model.nodes.*
import org.w3c.dom.Element
import kotlin.reflect.KClass

open class DecisionTreeXMLBuildException : XMLBuildException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Exception) : super(message, cause)
}

/**
 * Общее поведение для объектов-построителей дерева решений
 */
sealed class AbstractDecisionTreeXMLBuilder<T : DecisionTreeElement> : XMLBuilder<ElementBuildContext, T>() {

    override fun createException(message: String): XMLBuildException {
        return DecisionTreeXMLBuildException(message)
    }

    override fun createBuildContext(el: Element, buildClass: KClass<*>): ElementBuildContext {
        return ElementBuildContext(el, buildClass)
    }

    protected companion object {
        const val OUTCOME_TAG = "Outcome"
        const val EXPR_TAG = "Expression"
        const val DECISION_TREE_VAR_DECL_TAG = "DecisionTreeVarDecl"
        const val ADDITIONAL_DECISION_TREE_VAR_DECL_TAG = "AdditionalVarDecl"
        const val THOUGHT_BRANCH_TAG = "ThoughtBranch"

        const val VALUE_ATTR = "value"
        const val NAME_ATTR = "name"
        const val TYPE_ATTR = "type"
        const val LOGICAL_OP_ATTR = "operator"

        const val ADDITIONAL_INFO_PREFIX = "_"
    }

    protected fun Element.toExpr(): Operator = ExpressionXMLBuilder.build(this)

    protected fun <T : DecisionTreeElement> T.collectMetadata(el: Element): T {
        for (i in 0 until el.attributes.length) {
            val attr = el.attributes.item(i)
            if (attr.nodeName.startsWith(ADDITIONAL_INFO_PREFIX)) {
                val (locCode, metaPropertyName) = DomainBuilderUtils.splitMetadataPropertyName(
                    attr.nodeName.drop(ADDITIONAL_INFO_PREFIX.length),
                    delimiter = '.'
                )

                metadata.add(locCode, metaPropertyName, attr.nodeValue)
            }
        }
        return this
    }

    protected fun buildTypedVariable(el: Element): TypedVariable {
        val el = createBuildContext(el, TypedVariable::class)
        val name = el.getRequiredAttribute(NAME_ATTR)
        val type = el.getRequiredAttribute(TYPE_ATTR)
        return TypedVariable(type, name)
    }

    protected fun buildVarAssignment(el: Element): DecisionTreeVarAssignment {
        val el = createBuildContext(el, DecisionTreeVarAssignment::class)
        val variable = DecisionTreeNodeXMLBuilder.buildTypedVariable(el.el)
        val valueExpr = el.getRequiredSingleByWrapper(EXPR_TAG).toExpr()

        return DecisionTreeVarAssignment(variable, valueExpr).collectMetadata(el)
    }

    protected fun buildThoughtBranch(el: Element): ThoughtBranch {
        val el = createBuildContext(el, ThoughtBranch::class)
        val start = DecisionTreeNodeXMLBuilder.buildFromElement(el.getRequiredChild())
        return ThoughtBranch(start).collectMetadata(el)
    }

}

/**
 * Построение дерева решений ([DecisionTree]) из XML представления
 */
object DecisionTreeXMLBuilder : AbstractDecisionTreeXMLBuilder<DecisionTree>() {
    /**
     * Создает дерево решений из строки с XML
     */
    @JvmStatic
    fun fromXMLString(str: String) = buildFromXMLString(str)

    /**
     * Создает дерево решений из XML файла
     */
    @JvmStatic
    fun fromXMLFile(path: String) = buildFromXMLFile(path)

    /**
     * Создает дерево решений из узла XML
     */
    @JvmStatic
    fun build(el: Element) = buildFromElement(el)

    @BuildForTags(["StartNode", "DecisionTree"])
    @BuildingClass(DecisionTree::class)
    private fun buildDecisionTree(el: ElementBuildContext): DecisionTree {
        val variableWrapper = el.findChild("InputVariables")
        val parameters =
            variableWrapper?.getChildren(DECISION_TREE_VAR_DECL_TAG)?.map { buildTypedVariable(it) } ?: listOf()
        val implicitParameters =
            variableWrapper?.getChildren(ADDITIONAL_DECISION_TREE_VAR_DECL_TAG)?.map { buildVarAssignment(it) }
                ?: listOf()
        val branch = buildThoughtBranch(el.getRequiredChild(THOUGHT_BRANCH_TAG))

        return DecisionTree(parameters, implicitParameters, branch)
    }
}

/**
 * Построение узлов дерева решений ([DecisionTreeNode]) из XML представления
 */
object DecisionTreeNodeXMLBuilder : AbstractDecisionTreeXMLBuilder<DecisionTreeNode>() {
    /**
     * Создает узел дерева решений из строки с XML
     */
    @JvmStatic
    fun fromXMLString(str: String) = buildFromXMLString(str)

    /**
     * Создает узел дерева решений из XML файла
     */
    @JvmStatic
    fun fromXMLFile(path: String) = buildFromXMLFile(path)

    /**
     * Создает узел дерева решений из узла XML
     */
    @JvmStatic
    fun build(el: Element) = buildFromElement(el)

    private fun String.parseTupleValue(): Any? {
        if (this.startsWith('(') && this.endsWith(')'))
            throw createException("Cannot have tuples inside tuples")
        if (this == "*") return null
        return this.parseValue();
    }

    private fun String.parseValue(): Any {
        if (this.lowercase().toBooleanStrictOrNull() != null) return this.lowercase().toBooleanStrict()
        if (this.toIntOrNull() != null) return this.toInt()
        if (this.toDoubleOrNull() != null) return this.toDouble()
        if (this.matches("\\w+:\\w+".toRegex())) {
            val split = this.split(":", limit = 2)
            return EnumValue(split[0], split[1])
        }
        if (this.matches("class \\w+".toRegex())) {
            val split = this.split(" ", limit = 2)
            return Clazz(split[1])
        }
        if (this.matches("obj \\w+".toRegex())) {
            val split = this.split(" ", limit = 2)
            return Obj(split[1])
        }
        if (BranchResult.entries.map { it.name }.contains(this.uppercase())) {
            return BranchResult.valueOf(this.uppercase())
        }
        if (this.startsWith('(') && this.endsWith(')')) {
            return ValueTuple(
                this.substring(1, length - 1)
                    .split(';')
                    .map { it.trim().parseTupleValue() }
            )
        }
        return this
    }

    private fun <T : Any> ElementBuildContext.getRequiredAttributeAs(attr: String, type: KClass<T>): T {
        val valueStr = getRequiredAttribute(attr)
        var value = valueStr.parseValue()
        if(value is String && type == Boolean::class) { //FIXME Обратная совместимость
            value = (value.lowercase() == "found")
        }
        if (value is Boolean && type == BranchResult::class) { //FIXME Обратная совместимость
            value = if (value) BranchResult.CORRECT else BranchResult.ERROR
        }
        if (!type.isInstance(value)) {
            throw createException(
                "$this must have an attribute '$attr' that is parsable as a ${type.simpleName}, " +
                        "but it was '$valueStr', which cannot be parsed as a ${type.simpleName}"
            )
        }
        return value as T
    }

    private fun <T : Any> ElementBuildContext.getOutcomes(keyType: KClass<T>): Outcomes<T> {
        return Outcomes(getChildren(OUTCOME_TAG).map {
            buildOutcome(createBuildContext(it, this.buildClass), keyType)
        })
    }

    private fun <T : Any> buildOutcome(el: ElementBuildContext, keyType: KClass<T>): Outcome<T> {
        val key = el.getRequiredAttributeAs(VALUE_ATTR, keyType)

        if (!keyType.isInstance(key))
            throw createException("$el must have a key of type ${keyType.simpleName}, but was ${key::class.simpleName}")

        val nodeEl = el.getChildren().filter { it.tagName != THOUGHT_BRANCH_TAG }
        if (nodeEl.size != 1)
            throw createException(
                "$el must have exactly one child tag corresponding to the next node, " +
                        "but ${nodeEl.size} were found: $nodeEl"
            )
        val node = buildFromElement(nodeEl.single())

        return Outcome(key, node).collectMetadata(el)
    }

    @BuildForTags(["BranchResultNode"])
    @BuildingClass(BranchResultNode::class)
    private fun buildBranchResultNode(el: ElementBuildContext): BranchResultNode {
        val value = el.getRequiredAttributeAs(VALUE_ATTR, BranchResult::class)
        val expr = el.findSingleByWrapper(EXPR_TAG)?.toExpr()

        return BranchResultNode(value, expr).collectMetadata(el)
    }


    @BuildForTags(["LogicAggregationNode", "BranchAggregationNode"]) //FIXME обратная совместимость
    @BuildingClass(BranchAggregationNode::class)
    private fun buildBranchAggregationNode(el: ElementBuildContext): BranchAggregationNode {
        val op = AggregationMethod.fromString(el.getRequiredAttribute(LOGICAL_OP_ATTR))
            ?: throw createException("$el must have a valid logical operator described in its '$LOGICAL_OP_ATTR' attribute")

        val branches = el.getChildren(THOUGHT_BRANCH_TAG).map { buildThoughtBranch(it) }
        val outcomes = el.getOutcomes(BranchResult::class)

        return BranchAggregationNode(op, branches, outcomes).collectMetadata(el)
    }

    @BuildForTags(["CycleAggregationNode"])
    @BuildingClass(CycleAggregationNode::class)
    private fun buildCycleAggregationNode(el: ElementBuildContext): CycleAggregationNode {
        val op = AggregationMethod.fromString(el.getRequiredAttribute(LOGICAL_OP_ATTR))
            ?: throw createException("$el must have a valid logical operator described in its '$LOGICAL_OP_ATTR' attribute")
        val selector = el.getRequiredSingleByWrapper("SelectorExpression").toExpr()
        val variable = buildTypedVariable(el.getRequiredChild(DECISION_TREE_VAR_DECL_TAG))
        val errors = el.getChildren("FindError").map {
            buildFindErrorCategory(createBuildContext(it, FindErrorCategory::class), variable.className)
        }
        val branch = buildThoughtBranch(el.getRequiredChild(THOUGHT_BRANCH_TAG))
        val outcomes = el.getOutcomes(BranchResult::class)
        return CycleAggregationNode(op, selector, variable, errors, branch, outcomes).collectMetadata(el)
    }

    @BuildForTags(["WhileAggregationNode", "WhileCycleNode"]) //FIXME обратная совместимость
    @BuildingClass(WhileCycleNode::class)
    private fun buildWhileAggregationNode(el: ElementBuildContext): WhileCycleNode {
        val selector = el.getRequiredSingleByWrapper("SelectorExpression").toExpr()
        val branch = buildThoughtBranch(el.getRequiredChild(THOUGHT_BRANCH_TAG))
        val outcomes = el.getOutcomes(BranchResult::class)
        return WhileCycleNode(selector, branch, outcomes).collectMetadata(el)
    }


    @BuildForTags(["QuestionNode"])
    @BuildingClass(QuestionNode::class)
    private fun buildQuestionNode(el: ElementBuildContext): QuestionNode {
        val expr = el.getRequiredSingleByWrapper(EXPR_TAG).toExpr()
        val isSwitch = el.findAttribute("isSwitch")?.toBoolean() ?: false
        val outcomes = el.getOutcomes(Any::class)
        val trivialityExpr: Operator? = el.findSingleByWrapper("Triviality")?.toExpr()
        return QuestionNode(expr, outcomes, isSwitch, trivialityExpr).collectMetadata(el)
    }

    @BuildForTags(["TupleQuestionNode"])
    @BuildingClass(TupleQuestionNode::class)
    private fun buildTupleQuestionNode(el: ElementBuildContext): TupleQuestionNode {
        val parts = el.getChildren("Part")
            .map { buildTupleQuestionNodePart(createBuildContext(it, TupleQuestionNode.TupleQuestionPart::class)) }
        val outcomes = el.getOutcomes(ValueTuple::class)
        return TupleQuestionNode(parts, outcomes).collectMetadata(el)
    }

    private fun buildTupleQuestionNodePart(el: ElementBuildContext): TupleQuestionNode.TupleQuestionPart {
        val expr = el.getRequiredSingleByWrapper(EXPR_TAG).toExpr()
        val outcomes = el.getChildren(OUTCOME_TAG)
            .map {
                TupleQuestionNode.TupleQuestionOutcome(it.getAttribute(VALUE_ATTR).parseValue()).collectMetadata(it)
            }
        return TupleQuestionNode.TupleQuestionPart(expr, outcomes).collectMetadata(el)
    }

    private fun buildFindErrorCategory(el: ElementBuildContext, parentTypeName: String): FindErrorCategory {
        val priority = el.getRequiredAttributeAs("priority", Int::class)
        val expr = el.getRequiredSingleByWrapper(EXPR_TAG).toExpr()
        val varType = el.findAttribute(TYPE_ATTR) ?: parentTypeName
        val varName = el.findAttribute("checkedVar") ?: FindErrorCategory.CHECKED_OBJ

        return FindErrorCategory(priority, expr, TypedVariable(varType, varName))
            .collectMetadata(el)
    }

    @BuildForTags(["FindActionNode"])
    @BuildingClass(FindActionNode::class)
    private fun buildFindActionNode(el: ElementBuildContext): FindActionNode {
        val expr = el.getRequiredSingleByWrapper(EXPR_TAG).toExpr()
        val variable = buildTypedVariable(el.getRequiredChild(DECISION_TREE_VAR_DECL_TAG))
        val mainAssignment = DecisionTreeVarAssignment(variable, expr)

        val errors = el.getChildren("FindError").map {
            buildFindErrorCategory(createBuildContext(it, FindErrorCategory::class), variable.className)
        }

        val secondaryAssignments = el.getChildren(ADDITIONAL_DECISION_TREE_VAR_DECL_TAG).map {
            buildVarAssignment(it)
        }
        val outcomes = el.getOutcomes(Boolean::class)

        return FindActionNode(mainAssignment, errors, secondaryAssignments, outcomes).collectMetadata(el)
    }
}