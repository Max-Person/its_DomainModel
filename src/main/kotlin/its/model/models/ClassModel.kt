package its.model.models

import its.model.expressions.Operator
import its.model.expressions.xml.ExpressionXMLBuilder

/**
 * Модель класса в предметной области
 * @param name Имя класса
 * @param parent Имя класса родителя
 * @param calcExprXML Выражение для вычисления в формате XML
 */
open class ClassModel(
    val name: String,
    val parent: String? = null,
    private val calcExprXML: String? = null
) {

    /**
     * Проверяет корректность модели
     * @throws IllegalArgumentException
     */
    open fun validate() {
        require(name.isNotBlank()) {
            "Некорректное имя класса."
        }
    }

    val isCalculated get() = calcExprXML != null

    /**
     * Выражение для вычисления
     */
    val calcExpr
        get() = if (calcExprXML != null) ExpressionXMLBuilder.fromXMLString(calcExprXML) else null
}