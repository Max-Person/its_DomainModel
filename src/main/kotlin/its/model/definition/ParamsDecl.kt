package its.model.definition

import its.model.definition.types.Type
import mp.utils.Association

/**
 * Определение набора параметров для свойств и отношений
 */
class ParamsDecl : Association<String, ParamDecl> {
    constructor()
    constructor(elements: Collection<ParamDecl>) : super(elements)

    override fun getKey(element: ParamDecl) = element.name
}

/**
 * Определение параметра для свойств и отношений
 * @param name название параметра
 * @param type тип значения параметра
 */
data class ParamDecl(
    val name: String,
    val type: Type<*>,
)

