package its.model.nodes

import mp.utils.Association

/**
 * Переход из узла дерева мысли ([LinkNode]) в некоторый следующий узел [node]
 * @param key ключ перехода - значение, привязанное к переходу, играющее роль условия перехода (в разных узлах по разному)
 * @param node узел, к которому совершается переход
 */
class Outcome<V>(
    val key: V,
    val node: DecisionTreeNode,
) : DecisionTreeElement() {
    override val linkedElements: List<DecisionTreeElement>
        get() = mutableListOf<DecisionTreeElement>(node)
            .also { if (key is DecisionTreeElement) it.add(key) }
}

/**
 * Набор переходов [Outcome] из узла
 */
class Outcomes<V>(outcomes: Collection<Outcome<V>>) : Association<V, Outcome<V>>(outcomes) {
    override fun getKey(element: Outcome<V>) = element.key
}