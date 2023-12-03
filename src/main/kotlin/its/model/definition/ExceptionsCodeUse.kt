package its.model.definition

/**
 * Неправильные вызовы в моделях
 * Исключение "для программиста" - при корректном использовании кода не должно возникать никогда
 */
open class ModelMisuseException : IllegalArgumentException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Убедиться в правильном использовании кода ([requirement]), и выкинуть [ModelMisuseException] в случае ошибки
 */
internal fun preventMisuse(requirement: Boolean, message: String) {
    if (!requirement) {
        throw ModelMisuseException(message)
    }
}

/**
 * Ты не должен увидеть это исключение если все работает корректно
 */
class ThisShouldNotHappen : IllegalStateException()