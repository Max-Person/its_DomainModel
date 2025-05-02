package its.model.expressions.xml

import its.model.build.xml.XMLWriter
import its.model.expressions.Operator
import its.model.expressions.literals.*
import its.model.expressions.operators.*
import its.model.expressions.utils.NamedParamsValuesExprList
import its.model.expressions.utils.OrderedParamsValuesExprList
import its.model.expressions.utils.ParamsValuesExprList
import its.model.expressions.visitors.OperatorBehaviour
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.Writer

/**
 * Запись выражений ([Operator]) в виде XML
 * @see ExpressionXMLBuilder
 */
class ExpressionXMLWriter(document: Document) : XMLWriter(document), OperatorBehaviour<Element> {

    companion object {

        /**
         * Преобразовать переданное выражение [expression] в [Element] внутри переданного [document]
         */
        @JvmStatic
        fun expressionToElement(expression: Operator, document: Document) : Element {
            return expression.use(ExpressionXMLWriter(document))
        }

        /**
         * Записать переданное выражение [expression] в [writer] в формате xml
         */
        @JvmStatic
        fun writeExpressionToXml(expression: Operator, writer: Writer) {
            writeToXml(writer) { document -> expressionToElement(expression, document) }
        }

        /**
         * Преобразовать переданное выражение [expression] в xml-строку
         */
        @JvmStatic
        fun expressionToXmlString(expression: Operator) : String {
            return writeToXmlString { document -> expressionToElement(expression, document) }
        }

        private const val NAME = "name"
        private const val TYPE = "type"
        private const val VALUE = "value"
        private const val VAR_NAME = "varName"
        private const val PROPERTY_NAME = "propertyName"
        private const val RELATIONSHIP_NAME = "relationshipName"

        private const val PARAMS_VALUES = "ParamsValues"
    }

    private fun Operator.createElement() : Element {
        return this.use(this@ExpressionXMLWriter)
    }

    private fun Element.withOperand(operand: Operator): Element {
        return this.withChild(operand.createElement())
    }

    private fun createValueLiteralElement(tagName: String, literal: ValueLiteral<*, *>): Element {
        return newElement(tagName).withAttribute(VALUE, literal.value.toString())
    }

    override fun process(literal: BooleanLiteral) = createValueLiteralElement("Boolean", literal)
    override fun process(literal: DoubleLiteral) = createValueLiteralElement("Double", literal)
    override fun process(literal: IntegerLiteral) = createValueLiteralElement("Integer", literal)
    override fun process(literal: StringLiteral) = createValueLiteralElement("String", literal)

    override fun process(literal: EnumLiteral): Element {
        return newElement("Enum")
            .withAttribute("owner", literal.value.enumName)
            .withAttribute(VALUE, literal.value.valueName)
    }


    private fun createReferenceLiteralElement(tagName: String, literal: ReferenceLiteral): Element {
        return newElement(tagName).withAttribute(NAME, literal.name)
    }

    override fun process(literal: ClassLiteral) = createReferenceLiteralElement("Class", literal)
    override fun process(literal: ObjectLiteral) = createReferenceLiteralElement("Object", literal)
    override fun process(literal: DecisionTreeVarLiteral) = createReferenceLiteralElement("DecisionTreeVar", literal)
    override fun process(literal: VariableLiteral) = createReferenceLiteralElement("Variable", literal)

    override fun process(op: Cast): Element {
        return newElement("Cast")
            .withOperand(op.objectExpr)
            .withOperand(op.classExpr)
    }

    override fun process(op: CheckClass): Element {
        return newElement("CheckClass")
            .withOperand(op.objectExpr)
            .withOperand(op.classExpr)
    }

    override fun process(op: LogicalAnd): Element {
        return newElement("LogicalAnd")
            .withOperand(op.firstExpr)
            .withOperand(op.secondExpr)
    }

    override fun process(op: LogicalNot): Element {
        return newElement("LogicalNot")
            .withOperand(op.operandExpr)
    }

    override fun process(op: LogicalOr): Element {
        return newElement("LogicalOr")
            .withOperand(op.firstExpr)
            .withOperand(op.secondExpr)
    }

    override fun process(op: Compare): Element {
        return newElement("Compare")
            .withOperand(op.firstExpr)
            .withOperand(op.secondExpr)
    }

    override fun process(op: CompareWithComparisonOperator): Element {
        return newElement("Compare")
            .withAttribute("operator", op.operator.toString())
            .withOperand(op.firstExpr)
            .withOperand(op.secondExpr)
    }

    override fun process(op: GetClass): Element {
        return newElement("GetClass")
            .withOperand(op.objectExpr)
    }

    private fun ParamsValuesExprList.createElement(): Element {
        val paramsElement = newElement(PARAMS_VALUES)
        when(this){
            is NamedParamsValuesExprList -> {
                paramsElement.setAttribute(TYPE, "named")
                this.valuesMap.forEach { (paramName, paramValueExpr) ->
                    paramsElement.appendChild(
                        newElement("Param")
                            .withAttribute(NAME, paramName)
                            .withOperand(paramValueExpr)
                    )
                }
            }
            is OrderedParamsValuesExprList -> {
                paramsElement.setAttribute(TYPE, "ordered")
                this.values.forEach { paramValueExpr -> paramsElement.withOperand(paramValueExpr) }
            }
        }
        return paramsElement
    }

    private fun Element.withParams(params: ParamsValuesExprList) : Element {
        if(params.getExprList().isEmpty()){
            return this
        }
        return this.withChild(params.createElement())
    }

    override fun process(op: GetPropertyValue): Element {
        return newElement("GetPropertyValue")
            .withAttribute(PROPERTY_NAME, op.propertyName)
            .withOperand(op.objectExpr)
            .withParams(op.paramsValues)
    }

    override fun process(op: GetByRelationship): Element {
        return newElement("GetByRelationship")
            .withAttribute(RELATIONSHIP_NAME, op.relationshipName)
            .withOperand(op.subjectExpr)
            .withParams(op.paramsValues)
    }

    override fun process(op: CheckRelationship): Element {
        return newElement("CheckRelationship")
            .withAttribute(RELATIONSHIP_NAME, op.relationshipName)
            .withOperand(op.subjectExpr)
            .withParams(op.paramsValues)
            .apply { op.objectExprs.forEach { withOperand(it) } }
    }

    override fun process(op: GetRelationshipParamValue): Element {
        return newElement("GetRelationshipParamValue")
            .withAttribute(RELATIONSHIP_NAME, op.relationshipName)
            .withOperand(op.subjectExpr)
            .withParams(op.paramsValues)
            .apply { op.objectExprs.forEach { withOperand(it) } }
            .withAttribute("paramName", op.paramName)
    }

    override fun process(op: AssignProperty): Element {
        return newElement("AssignToProperty")
            .withOperand(op.objectExpr)
            .withAttribute(PROPERTY_NAME, op.propertyName)
            .withParams(op.paramsValues)
            .withOperand(op.valueExpr)
    }

    override fun process(op: AssignDecisionTreeVar): Element {
        return newElement("AssignToDecisionTreeVar")
            .withAttribute(VAR_NAME, op.variableName)
            .withOperand(op.valueExpr)
    }

    override fun process(op: AddRelationshipLink): Element {
        return newElement("AddRelationshipLink")
            .withAttribute(RELATIONSHIP_NAME, op.relationshipName)
            .withOperand(op.subjectExpr)
            .withParams(op.paramsValues)
            .apply { op.objectExprs.forEach { withOperand(it) } }
    }

    override fun process(op: ExistenceQuantifier): Element {
        return newElement("ExistenceQuantifier")
            .withAttribute(TYPE, op.variable.className)
            .withAttribute(VAR_NAME, op.variable.varName)
            .apply { if(op.selectorExpr!= null) withOperand(op.selectorExpr) }
            .withOperand(op.conditionExpr)
    }

    override fun process(op: ForAllQuantifier): Element {
        return newElement("ForAllQuantifier")
            .withAttribute(TYPE, op.variable.className)
            .withAttribute(VAR_NAME, op.variable.varName)
            .apply { if(op.selectorExpr!= null) withOperand(op.selectorExpr) }
            .withOperand(op.conditionExpr)
    }

    override fun process(op: GetByCondition): Element {
        return newElement("GetByCondition")
            .withAttribute(TYPE, op.variable.className)
            .withAttribute(VAR_NAME, op.variable.varName)
            .withOperand(op.conditionExpr)
    }

    override fun process(op: GetExtreme): Element {
        return newElement("GetExtreme")
            .withAttribute(TYPE, op.className)
            .withAttribute(VAR_NAME, op.varName)
            .withOperand(op.conditionExpr)
            .withAttribute("extremeVarName", op.extremeVarName)
            .withOperand(op.extremeConditionExpr)
    }

    override fun process(op: Block): Element {
        return newElement("Block")
            .apply { op.nestedExprs.forEach { withOperand(it) } }
    }

    override fun process(op: IfThen): Element {
        return newElement("IfThen")
            .withOperand(op.conditionExpr)
            .withOperand(op.thenExpr)
            .apply { if(op.elseExpr!= null) withOperand(op.elseExpr) }
    }
}