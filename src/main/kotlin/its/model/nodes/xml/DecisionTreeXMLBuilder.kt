package its.model.nodes.xml

import its.model.TypedVariable
import its.model.build.xml.ElementBuildContext
import its.model.build.xml.XMLBuildException
import its.model.build.xml.XMLBuilder
import its.model.definition.MetadataProperty
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
                val metadataName = attr.nodeName.drop(ADDITIONAL_INFO_PREFIX.length)
                    .replace(ADDITIONAL_INFO_PREFIX, MetadataProperty.LOC_CODE_DELIMITER)

                metadata.add(MetadataProperty(metadataName), attr.nodeValue)
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

    @BuildForTags(["StartNode"])
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
        return this
    }

    private fun <T : Any> ElementBuildContext.getRequiredAttributeAs(attr: String, type: KClass<T>): T {
        val valueStr = getRequiredAttribute(attr)
        val value = valueStr.parseValue()
        if (!type.isInstance(value)) {
            throw createException(
                "$this must have an attribute '$attr' that is parsable as a ${type.simpleName}, " +
                        "but it was '$valueStr', which cannot be parsed as a ${type.simpleName}"
            )
        }
        return value as T
    }

    private fun <T : Any> ElementBuildContext.getOutcomes(keyType: KClass<T>): Outcomes<T> {
        return getOutcomesNullable(keyType)
    }

    private fun <T : Any, T_act : T?> ElementBuildContext.getOutcomesNullable(keyType: KClass<T>): Outcomes<T_act> {
        return Outcomes(getChildren(OUTCOME_TAG).map {
            buildOutcome<T, T_act>(createBuildContext(it, this.buildClass), keyType)
        })
    }

    private fun <T : Any, T_act : T?> buildOutcome(el: ElementBuildContext, keyType: KClass<T>): Outcome<T_act> {
        val key = if (keyType == ThoughtBranch::class)
            el.findChild(THOUGHT_BRANCH_TAG)?.let { buildThoughtBranch(it) }
        else
            el.getRequiredAttributeAs(VALUE_ATTR, keyType)

        if (key != null && !keyType.isInstance(key))
            throw createException("$el must have a key of type ${keyType.simpleName}, but was ${key::class.simpleName}")
        key as T_act

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
    private fun buildBrachResultNode(el: ElementBuildContext): BranchResultNode {
        val value = el.getRequiredAttributeAs(VALUE_ATTR, Boolean::class)
        val expr = el.findSingleByWrapper(EXPR_TAG)?.toExpr()

        return BranchResultNode(value, expr).collectMetadata(el)
    }


    @BuildForTags(["LogicAggregationNode"])
    @BuildingClass(LogicAggregationNode::class)
    private fun buildLogicAggregationNode(el: ElementBuildContext): LogicAggregationNode {
        val op = LogicalOp.fromString(el.getRequiredAttribute(LOGICAL_OP_ATTR))
            ?: throw createException("$el must have a valid logical operator described in its '$LOGICAL_OP_ATTR' attribute")

        val branches = el.getChildren(THOUGHT_BRANCH_TAG).map { buildThoughtBranch(it) }
        val outcomes = el.getOutcomes(Boolean::class)

        return LogicAggregationNode(op, branches, outcomes).collectMetadata(el)
    }

    @BuildForTags(["CycleAggregationNode"])
    @BuildingClass(CycleAggregationNode::class)
    private fun buildCycleAggregationNode(el: ElementBuildContext): CycleAggregationNode {
        val op = LogicalOp.fromString(el.getRequiredAttribute(LOGICAL_OP_ATTR))
            ?: throw createException("$el must have a valid logical operator described in its '$LOGICAL_OP_ATTR' attribute")
        val selector = el.getRequiredSingleByWrapper("SelectorExpression").toExpr()
        val variable = buildTypedVariable(el.getRequiredChild(DECISION_TREE_VAR_DECL_TAG))
        val branch = buildThoughtBranch(el.getRequiredChild(THOUGHT_BRANCH_TAG))
        val outcomes = el.getOutcomes(Boolean::class)
        return CycleAggregationNode(op, selector, variable, branch, outcomes).collectMetadata(el)
    }

    @BuildForTags(["WhileAggregationNode"])
    @BuildingClass(WhileAggregationNode::class)
    private fun buildWhileAggregationNode(el: ElementBuildContext): WhileAggregationNode {
        val op = LogicalOp.fromString(el.getRequiredAttribute(LOGICAL_OP_ATTR))
            ?: throw createException("$el must have a valid logical operator described in its '$LOGICAL_OP_ATTR' attribute")
        val selector = el.getRequiredSingleByWrapper("SelectorExpression").toExpr()
        val branch = buildThoughtBranch(el.getRequiredChild(THOUGHT_BRANCH_TAG))
        val outcomes = el.getOutcomes(Boolean::class)
        return WhileAggregationNode(op, selector, branch, outcomes).collectMetadata(el)
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

    @BuildForTags(["PredeterminingFactorsNode"])
    @BuildingClass(PredeterminingFactorsNode::class)
    private fun buildPredeterminingFactorsNode(el: ElementBuildContext): PredeterminingFactorsNode {
        val outcomes = el.getOutcomesNullable(ThoughtBranch::class)
        return PredeterminingFactorsNode(outcomes)
    }


    private fun buildFindErrorCategory(el: ElementBuildContext): FindActionNode.FindErrorCategory {
        val priority = el.getRequiredAttributeAs("priority", Int::class)
        val expr = el.getRequiredSingleByWrapper(EXPR_TAG).toExpr()

        return FindActionNode.FindErrorCategory(priority, expr).collectMetadata(el)
    }

    @BuildForTags(["FindActionNode"])
    @BuildingClass(FindActionNode::class)
    private fun buildFindActionNode(el: ElementBuildContext): FindActionNode {
        val expr = el.getRequiredSingleByWrapper(EXPR_TAG).toExpr()
        val variable = buildTypedVariable(el.getRequiredChild(DECISION_TREE_VAR_DECL_TAG))
        val mainAssignment = DecisionTreeVarAssignment(variable, expr)

        val errors = el.getChildren("FindError").map {
            buildFindErrorCategory(createBuildContext(it, FindActionNode.FindErrorCategory::class))
        }

        val secondaryAssignments = el.getChildren(ADDITIONAL_DECISION_TREE_VAR_DECL_TAG).map {
            buildVarAssignment(it)
        }
        val outcomes = Outcomes(el.getOutcomes(String::class).map {
            Outcome(it.key.lowercase() == "found", it.node)
        })

        return FindActionNode(mainAssignment, errors, secondaryAssignments, outcomes).collectMetadata(el)
    }
}