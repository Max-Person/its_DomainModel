package its.model.expressions

import its.model.Describable
import its.model.definition.DomainModel
import its.model.definition.loqi.OperatorLoqiWriter
import its.model.definition.types.Type
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Оператор в логическом выражении (LOQI выражения)
 */
abstract class Operator : Cloneable, Describable {

    /**
     * Список аргументов
     */
    abstract val children: List<Operator>

    /**
     * Получить аргумент
     * @param index Индекс аргумента
     * @return Аргумент
     */
    fun arg(index: Int) = children[index]

    /**
     * Описание оператора
     */
    override val description
        get() = OperatorLoqiWriter.getWrittenExpression(this)

    override fun toString(): String {
        return description
    }

    /**
     * Динамически определяемый тип данных оператора
     */
    open fun resolvedType(domainModel: DomainModel, expressionContext: ExpressionContext = ExpressionContext()): Type<*> =
        validateAndGetType(domainModel, ExpressionValidationResults(true), expressionContext)

    /**
     * Валидация - провалидировать выражение (с учетом контекста [context]) и положить все потенциальные ошибки в [results]
     */
    internal abstract fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext,
    ): Type<*>

    fun validateAndGet(
        domainModel: DomainModel,
        context: ExpressionContext = ExpressionContext(),
    ): Pair<Type<*>, ExpressionValidationResults> {
        val results = ExpressionValidationResults()
        val type = validateAndGetType(domainModel, results, context)
        return type to results
    }

    /**
     * Валидация - провалидировать выражение (с учетом контекста [context])
     */
    fun validate(domainModel: DomainModel, context: ExpressionContext = ExpressionContext()) {
        validateAndGetType(domainModel, ExpressionValidationResults(true), context)
    }

    /**
     * Создает копию объекта
     * @return Копия
     */
    override fun clone(): Operator {
        return super.clone() as Operator
    }

    /**
     * Применяет поведение [behaviour] к данному оператору
     * @param behaviour применяемое поведение
     * @return информация, возвращаемая поведением при обработке данного оператору
     * @see OperatorBehaviour
     */
    abstract fun <I> use(behaviour: OperatorBehaviour<I>): I

    companion object
}