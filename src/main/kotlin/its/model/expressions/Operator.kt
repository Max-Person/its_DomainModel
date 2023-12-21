package its.model.expressions

import its.model.definition.Domain
import its.model.definition.types.Type
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Оператор в логическом выражении (LOQI выражения)
 */
abstract class Operator : Cloneable {

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
    open val description
        get() = "a ${this::class.simpleName} operator"

    /**
     * Динамически определяемый тип данных оператора
     */
    open fun resolvedType(domain: Domain): Type<*> =
        validateAndGetType(domain, ExpressionValidationResults(true), ExpressionContext())

    /**
     * Валидация - провалидировать выражение (с учетом контекста [context]) и положить все потенциальные ошибки в [results]
     */
    internal abstract fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext,
    ): Type<*>

    /**
     * Валидация - провалидировать выражение (с учетом контекста [context])
     */
    fun validate(domain: Domain, context: ExpressionContext = ExpressionContext()) {
        validateAndGetType(domain, ExpressionValidationResults(true), context)
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