package its.model

import its.model.definition.Domain
import its.model.nodes.DecisionTreeContext

data class TypedVariable(
    val className: String,
    val varName: String,
) {
    fun checkValid(
        domain: Domain,
        results: DomainConstructValidationResults,
        context: DecisionTreeContext,
        owner: Describable,
    ): Boolean {
        var valid = true
        if (domain.classes.get(className).isEmpty) {
            results.nonConforming(
                "No class of name '$className' found in domain, " +
                        "but it was declared as a type for variable in ${owner.description}"
            )
            valid = false
        }

        if (context.variableTypes.containsKey(varName)) {
            results.invalid(
                "Variable $varName of type $className declared in ${owner.description} " +
                        "shadows variable $varName of type ${context.variableTypes[varName]} declared previously"
            )
            valid = false
        }

        return valid
    }
}