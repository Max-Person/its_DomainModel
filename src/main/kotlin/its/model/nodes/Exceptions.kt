package its.model.nodes

import its.model.DomainConstructValidationResults
import its.model.InvalidDomainConstructException

open class InvalidDecisionTreeException : InvalidDomainConstructException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

class DecisionTreeValidationResults(throwImmediately: Boolean = false) :
    DomainConstructValidationResults(throwImmediately) {
    override fun createInvalid(message: String): InvalidDomainConstructException {
        return InvalidDecisionTreeException(message)
    }
}