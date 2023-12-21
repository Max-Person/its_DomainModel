package its.model.expressions

import its.model.DomainConstructValidationResults
import its.model.InvalidDomainConstructException

open class InvalidExpressionException : InvalidDomainConstructException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

class ExpressionValidationResults(throwImmediately: Boolean = false) :
    DomainConstructValidationResults(throwImmediately) {
    override fun createInvalid(message: String): InvalidDomainConstructException {
        return InvalidExpressionException(message)
    }
}