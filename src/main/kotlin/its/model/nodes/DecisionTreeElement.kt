package its.model.nodes

import its.model.Describable
import its.model.definition.DomainModel
import its.model.definition.MetaData
import its.model.definition.MetaOwner
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.Operator
import java.util.*
import kotlin.properties.Delegates

/**
 * Элемент дерева решений [DecisionTree]
 */
sealed class DecisionTreeElement : MetaOwner, Describable {
    override val metadata = MetaData(this)

    /**
     * Ссылка на дерево решений, к которому принадлежит данный элемент
     */
    var decisionTree: DecisionTree by Delegates.notNull()

    /**
     * Ссылка на родительский элемент в дереве
     */
    var parent: DecisionTreeElement by Delegates.notNull()

    /**
     * Элементы дерева, связанные с текущим, "дочерние элементы"
     */
    abstract val linkedElements: List<DecisionTreeElement>


    /**
     * Настроить связи в дереве:
     * установить текущему элементу связь с родительским элементом и связь с деревом решений,
     * А также вызвать это рекурсивно
     */
    protected fun setupLinks(parent: DecisionTreeElement) {
        this.parent = parent
        this.decisionTree = parent.decisionTree
        linkedElements.forEach {
            it.setupLinks(this)
        }
    }


    /**
     * Строковое описание элемента
     */
    override val description: String
        get() {
            val className = this::class.simpleName
            var descr = metadata["alias"]?.toString()
                ?: metadata["label"]?.toString()
                ?: ""
            if (descr.isNotBlank())
                descr = " '$descr'"
            return className + descr
        }

    override fun toString() = description

    /**
     * Валидация - провалидировать дерево решений (с учетом контекста [context]) и положить все потенциальные ошибки в [results]
     */
    internal open fun validate(
        domainModel: DomainModel,
        results: DecisionTreeValidationResults,
        context: DecisionTreeContext,
    ) {
        validateLinked(domainModel, results, context)
    }

    protected fun validateLinked(
        domainModel: DomainModel,
        results: DecisionTreeValidationResults,
        context: DecisionTreeContext,
        linked: List<DecisionTreeElement> = linkedElements,
    ) {
        linked.forEach { it.validate(domainModel, results, context) }
    }

    protected fun Operator.validateForDecisionTree(
        domainModel: DomainModel,
        results: DecisionTreeValidationResults,
        context: DecisionTreeContext,
        withVariables: Map<String, String> = emptyMap(),
    ): Type<*> {
        val (exprType, exprResults) = this.validateAndGet(
            domainModel,
            ExpressionContext.from(context).apply { variableTypes.putAll(withVariables) }
        )
        results.addAll(exprResults)
        return exprType
    }

    /**
     * Валидация - провалидировать дерево решений (с учетом контекста [context]) и получить все ошибки
     */
    fun validateAndGet(
        domainModel: DomainModel,
        context: DecisionTreeContext = DecisionTreeContext()
    ): DecisionTreeValidationResults {
        return DecisionTreeValidationResults().also { validate(domainModel, it, context) }
    }

    /**
     * Валидация - провалидировать дерево решений (с учетом контекста [context]) и выкинуть все ошибки
     * @throws InvalidDecisionTreeException в случае невалидности конструкций в дереве
     * @throws DomainNonConformityException в случае несоответствий конструкций дерева объявленным в домене определениям
     */
    fun validate(domainModel: DomainModel, context: DecisionTreeContext = DecisionTreeContext()) {
        validate(domainModel, DecisionTreeValidationResults(true), context)
    }
}

sealed class HelperDecisionTreeElement : DecisionTreeElement() {
    override val linkedElements: List<DecisionTreeElement>
        get() = emptyList()
}
