package its.model.definition

import its.model.Describable

/**
 * Любой элемент домена
 *
 * Имеет ссылку на домен, в котором содержится, и функции для валидации
 */
abstract class DomainElement : Describable {
    /**
     * Домен, в котором содержится элемент
     */
    abstract val domain: Domain

    /**
     * Строковое описание элемента домена
     */
    override val description: String
        get() = "DomainElement"

    /**
     * Валидация - провалидировать элемент домена и положить все потенциальные ошибки в [results]
     */
    internal open fun validate(results: DomainValidationResults) {}

    /**
     * Валидация - провалидировать элемент домена и получить все потенциальные ошибки
     */
    fun validateAndGet(): DomainValidationResults {
        val results = DomainValidationResults()
        validate(results)
        return results
    }

    /**
     * Валидация - провалидировать элемент домена и выкинуть невалидное как исключения
     * @throws InvalidDomainDefinitionException при невалидной модели
     */
    fun validateAndThrowInvalid() = validateAndGet().throwInvalid()

    /**
     * Валидация - провалидировать элемент домена и выкинуть все потенциальные ошибки
     * @throws InvalidDomainDefinitionException при невалидной модели
     * @throws UnknownDomainDefinitionException при недостаточных данных в модели
     */
    fun validateAndThrow() = validateAndGet().throwAll()
}

