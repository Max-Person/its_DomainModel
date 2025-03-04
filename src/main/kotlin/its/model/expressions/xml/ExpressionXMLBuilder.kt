package its.model.expressions.xml

import its.model.TypedVariable
import its.model.build.xml.ElementBuildContext
import its.model.build.xml.XMLBuildException
import its.model.build.xml.XMLBuilder
import its.model.definition.ThisShouldNotHappen
import its.model.definition.types.Comparison
import its.model.definition.types.EnumValue
import its.model.expressions.Operator
import its.model.expressions.getTypesFromConditionExpr
import its.model.expressions.literals.*
import its.model.expressions.operators.*
import org.w3c.dom.Element
import kotlin.reflect.KClass

/**
 * Построение выражений ([Operator]) из XML представления
 */
object ExpressionXMLBuilder : XMLBuilder<ExpressionXMLBuilder.ExpressionBuildContext, Operator>() {

    /**
     * Создает выражение из строки с XML
     * @param str Строка с XML
     * @return Выражение
     */
    @JvmStatic
    fun fromXMLString(str: String) = buildFromXMLString(str)

    /**
     * Создает выражение из XML файла
     * @param path Путь к файлу
     * @return Выражение
     */
    @JvmStatic
    fun fromXMLFile(path: String) = buildFromXMLFile(path)

    /**
     * Создает оператор из узла XML
     * @param el XML узел
     * @return Оператор
     */
    @JvmStatic
    fun build(el: Element): Operator = buildFromElement(el)

    //--- Утилитарное ---

    private const val NAME = "name"
    private const val TYPE = "type"
    private const val VALUE = "value"
    private const val VAR_NAME = "varName"
    private const val PROPERTY_NAME = "propertyName"
    private const val RELATIONSHIP_NAME = "relationshipName"

    class ExpressionBuildContext(
        el: Element,
        buildClass: KClass<*>,
        var operands: List<Operator>,
    ) : ElementBuildContext(el, buildClass)

    override fun createException(message: String) = ExpressionXMLBuildException(message)

    override fun createBuildContext(el: Element, buildClass: KClass<*>): ExpressionBuildContext {
        val operands = el.getChildren().map { build(it) }
        return ExpressionBuildContext(el, buildClass, operands)
    }

    override val defaultBuildingClass: KClass<*>
        get() = Operator::class

    private fun ExpressionBuildContext.op(index: Int): Operator {
        if (operands.size <= index)
            throw createException("$this must have an operand at index $index")
        return operands[index]
    }

    private fun ExpressionBuildContext.getAttributeOrTakeFromChild(
        attr: String,
        childIndex: Int,
    ): String {
        val el = operands.toMutableList()
        return this.findAttribute(attr)
            ?: run {
                if (operands.size <= childIndex
                    || operands[childIndex].let { it !is ValueLiteral<*, *> && it !is ReferenceLiteral }
                )
                    throw createException(
                        "$this must either have a '$attr' attribute " +
                                "or have a named operand (child tag) at index $childIndex"
                    )
                val child = el.removeAt(childIndex)
                operands = el
                when (child) {
                    is ValueLiteral<*, *> -> child.value.toString()
                    is ReferenceLiteral -> child.name
                    else -> throw ThisShouldNotHappen()
                }
            }
    }

    private fun ExpressionBuildContext.getTypeFromConditionExpr(condition: Operator, varName: String): String {
        val types = getTypesFromConditionExpr(condition, varName)
        if (types.size != 1) {
            throw createException("Cannot infer type for variable '$varName' in $this")
        }
        return types.single()
    }

    //--- Построение ---

    @BuildForTags(["Variable"])
    @BuildingClass(VariableLiteral::class)
    private fun buildVariableLiteral(el: ExpressionBuildContext): VariableLiteral {
        val name = el.getRequiredAttribute(NAME)
        return VariableLiteral(name)
    }

    @BuildForTags(["DecisionTreeVar"])
    @BuildingClass(DecisionTreeVarLiteral::class)
    private fun buildDecisionTreeVarLiteral(el: ExpressionBuildContext): DecisionTreeVarLiteral {
        val name = el.getRequiredAttribute(NAME)
        return DecisionTreeVarLiteral(name)
    }

    @BuildForTags(["Class"])
    @BuildingClass(ClassLiteral::class)
    private fun buildClassLiteral(el: ExpressionBuildContext): ClassLiteral {
        val name = el.getRequiredAttribute(NAME)
        return ClassLiteral(name)
    }

    @BuildForTags(["Object"])
    @BuildingClass(ObjectLiteral::class)
    private fun buildObjectLiteral(el: ExpressionBuildContext): ObjectLiteral {
        val name = el.getRequiredAttribute(NAME)
        return ObjectLiteral(name)
    }

    @BuildForTags(["ComparisonResult"])
    @BuildingClass(EnumLiteral::class)
    private fun buildComparisonResultLiteral(el: ExpressionBuildContext): EnumLiteral {
        val value = el.getRequiredAttribute(VALUE)
        return EnumLiteral(EnumValue(Comparison.Type.enumName, value))
    }

    @BuildForTags(["String"])
    @BuildingClass(StringLiteral::class)
    private fun buildString(el: ExpressionBuildContext): StringLiteral {
        val value = el.getRequiredAttribute(VALUE)
        return StringLiteral(value)
    }

    @BuildForTags(["Boolean"])
    @BuildingClass(BooleanLiteral::class)
    private fun buildBoolean(el: ExpressionBuildContext): BooleanLiteral {
        val value = el.getRequiredAttribute(VALUE)
        return BooleanLiteral(value.toBoolean())
    }

    @BuildForTags(["Integer"])
    @BuildingClass(IntegerLiteral::class)
    private fun buildInteger(el: ExpressionBuildContext): IntegerLiteral {
        val value = el.getRequiredAttribute(VALUE)
        return IntegerLiteral(value.toInt())
    }

    @BuildForTags(["Double"])
    @BuildingClass(DoubleLiteral::class)
    private fun buildDouble(el: ExpressionBuildContext): DoubleLiteral {
        val value = el.getRequiredAttribute(VALUE)
        return DoubleLiteral(value.toDouble())
    }

    @BuildForTags(["Enum"])
    @BuildingClass(EnumLiteral::class)
    private fun buildEnum(el: ExpressionBuildContext): EnumLiteral {
        val enum = el.getRequiredAttribute("owner")
        val value = el.getRequiredAttribute(VALUE)
        return EnumLiteral(EnumValue(enum, value))
    }

    @BuildForTags(["AssignToDecisionTreeVar"])
    @BuildingClass(AssignDecisionTreeVar::class)
    private fun buildAssignToDecisionTreeVar(el: ExpressionBuildContext): AssignDecisionTreeVar {
        val varName = el.getAttributeOrTakeFromChild(VAR_NAME, 0)
        return AssignDecisionTreeVar(
            varName,
            el.op(0)
        )
    }

    @BuildForTags(["AssignToProperty"])
    @BuildingClass(AssignProperty::class)
    private fun buildAssignToProperty(el: ExpressionBuildContext): AssignProperty {
        val propertyName = el.getAttributeOrTakeFromChild(PROPERTY_NAME, 1)
        return AssignProperty(
            el.op(0),
            propertyName,
            el.op(1),
        )
    }

    @BuildForTags(["AddRelationshipLink"])
    @BuildingClass(AddRelationshipLink::class)
    private fun buildAddRelationshipLink(el: ExpressionBuildContext): AddRelationshipLink {
        val relationshipName = el.getAttributeOrTakeFromChild(RELATIONSHIP_NAME, 0)
        return AddRelationshipLink(
            el.op(0),
            relationshipName,
            el.operands.subList(1, el.operands.size),
        )
    }

    @BuildForTags(["Cast"])
    @BuildingClass(Cast::class)
    private fun buildCast(el: ExpressionBuildContext): Cast {
        return Cast(
            el.op(0),
            el.op(1),
        )
    }

    @BuildForTags(["CheckClass"])
    @BuildingClass(CheckClass::class)
    private fun buildCheckClass(el: ExpressionBuildContext): CheckClass {
        return CheckClass(
            el.op(0),
            el.op(1),
        )
    }


    @BuildForTags(["CheckRelationship"])
    @BuildingClass(CheckRelationship::class)
    private fun buildCheckRelationship(el: ExpressionBuildContext): CheckRelationship {
        val relationshipName = el.getAttributeOrTakeFromChild(RELATIONSHIP_NAME, 0)
        return CheckRelationship(
            el.op(0),
            relationshipName,
            el.operands.subList(1, el.operands.size),
        )
    }

    @BuildForTags(["Compare"])
    private fun buildCompare(el: ExpressionBuildContext): Operator {
        val opString = el.findAttribute("operator")
        return if (opString != null) {
            el.buildClass = CompareWithComparisonOperator::class
            val operator = CompareWithComparisonOperator.ComparisonOperator.fromString(opString)
            CompareWithComparisonOperator(
                el.op(0),
                operator,
                el.op(1),
            )
        } else {
            el.buildClass = Compare::class
            Compare(
                el.op(0),
                el.op(1),
            )
        }
    }

    @BuildForTags(["ExistenceQuantifier"])
    @BuildingClass(ExistenceQuantifier::class)
    private fun buildExistenceQuantifier(el: ExpressionBuildContext): ExistenceQuantifier {
        val selector = el.op(0)
        val condition = el.op(1)
        val varName = el.getRequiredAttribute(VAR_NAME)
        val type = el.findAttribute(TYPE) ?: el.getTypeFromConditionExpr(selector, varName)
        return ExistenceQuantifier(TypedVariable(type, varName), selector, condition)
    }

    @BuildForTags(["ForAllQuantifier"])
    @BuildingClass(ForAllQuantifier::class)
    private fun buildForAllQuantifier(el: ExpressionBuildContext): ForAllQuantifier {
        val selector = el.op(0)
        val condition = el.op(1)
        val varName = el.getRequiredAttribute(VAR_NAME)
        val type = el.findAttribute(TYPE) ?: el.getTypeFromConditionExpr(selector, varName)
        return ForAllQuantifier(TypedVariable(type, varName), selector, condition)
    }

    @BuildForTags(["GetByCondition"])
    @BuildingClass(GetByCondition::class)
    private fun buildGetByCondition(el: ExpressionBuildContext): GetByCondition {
        val condition = el.op(0)
        val varName = el.getRequiredAttribute(VAR_NAME)
        val type = el.findAttribute(TYPE) ?: el.getTypeFromConditionExpr(condition, varName)
        return GetByCondition(TypedVariable(type, varName), condition)
    }

    @BuildForTags(["GetByRelationship"])
    @BuildingClass(GetByRelationship::class)
    private fun buildGetByRelationship(el: ExpressionBuildContext): GetByRelationship {
        val relationshipName = el.getAttributeOrTakeFromChild(RELATIONSHIP_NAME, 1)
        return GetByRelationship(
            el.op(0),
            relationshipName,
        )
    }

    @BuildForTags(["GetClass"])
    @BuildingClass(GetClass::class)
    private fun buildGetClass(el: ExpressionBuildContext): GetClass {
        return GetClass(el.op(0))
    }

    @BuildForTags(["GetExtreme"])
    @BuildingClass(GetExtreme::class)
    private fun buildGetExtreme(el: ExpressionBuildContext): GetExtreme {
        val extremeCondition = el.op(0)
        val condition = el.op(1)
        val extremeVarName = el.getRequiredAttribute("extremeVarName")
        val varName = el.getRequiredAttribute(VAR_NAME)
        val type = el.findAttribute(TYPE) ?: el.getTypeFromConditionExpr(condition, varName)
        return GetExtreme(type, varName, condition, extremeVarName, extremeCondition)
    }

    @BuildForTags(["GetPropertyValue"])
    @BuildingClass(GetPropertyValue::class)
    private fun buildGetPropertyValue(el: ExpressionBuildContext): GetPropertyValue {
        val propertyName = el.getAttributeOrTakeFromChild(PROPERTY_NAME, 1)
        return GetPropertyValue(
            el.op(0),
            propertyName,
        )
    }

    @BuildForTags(["LogicalAnd"])
    @BuildingClass(LogicalAnd::class)
    private fun buildLogicalAnd(el: ExpressionBuildContext): LogicalAnd {
        return LogicalAnd(
            el.op(0),
            el.op(1),
        )
    }

    @BuildForTags(["LogicalOr"])
    @BuildingClass(LogicalOr::class)
    private fun buildLogicalOr(el: ExpressionBuildContext): LogicalOr {
        return LogicalOr(
            el.op(0),
            el.op(1),
        )
    }

    @BuildForTags(["LogicalNot"])
    @BuildingClass(LogicalNot::class)
    private fun buildLogicalNot(el: ExpressionBuildContext): LogicalNot {
        return LogicalNot(
            el.op(0),
        )
    }

    @BuildForTags(["With"])
    @BuildingClass(With::class)
    private fun buildWith(el: ExpressionBuildContext): With {
        val varName = el.getRequiredAttribute(VAR_NAME)
        return With(
            el.op(0),
            varName,
            el.op(1),
        )
    }

    @BuildForTags(["Block"])
    @BuildingClass(Block::class)
    private fun buildBlock(el: ExpressionBuildContext): Block {
        return Block(
            el.operands,
        )
    }

    @BuildForTags(["IfThen"])
    @BuildingClass(IfThen::class)
    private fun buildIfThen(el: ExpressionBuildContext): IfThen {
        return IfThen(
            el.op(0),
            el.op(1),
            el.operands.getOrNull(2)
        )
    }

    override fun buildDefault(el: ExpressionBuildContext): Operator {
        //В случае неизвестного типа узла собираем имя в строковый литерал
        //Это нужно чтобы данные из устаревших типов операторов (ссылка на свойство и т.п.) не потерялись
        return el.findAttribute(NAME)
            ?.let { StringLiteral(it) }
            .orElseBuildErr("No build functions exist for tags '${el.nodeName}' to construct an Operator.")
    }

}

open class ExpressionXMLBuildException : XMLBuildException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Exception) : super(message, cause)
}