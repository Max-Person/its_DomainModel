package its.model.expressions

interface HasContext {

    val context: MutableSet<String>

    fun addToContext(operator: Operator, variable: String) {
        with(operator) {
            context.add(variable)

            children.forEach {
                if (it is HasContext) {
                    it.addToContext(it, variable)
                } else {
                    rec(variable)
                }
            }
        }
    }
}

fun Operator.rec(variable: String) {
    children.forEach {
        if (it is HasContext) {
            it.addToContext(it, variable)
        } else {
            it.rec(variable)
        }
    }
}
