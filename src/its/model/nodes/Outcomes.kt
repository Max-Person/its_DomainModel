package its.model.nodes

class Outcomes<KeyType>(private val next : Map<KeyType, Pair<DecisionTreeNode, Map<String, String>>>) : Map<KeyType, DecisionTreeNode>{

    class Outcome<KeyType>(override val key: KeyType, override val value: DecisionTreeNode) : Map.Entry<KeyType, DecisionTreeNode>

    override operator fun get(key: KeyType) : DecisionTreeNode?{
        return next[key]?.first
    }

    fun additionalInfo(key: KeyType) : Map<String, String>?{
        return next[key]?.second
    }

    override val entries: Set<Map.Entry<KeyType, DecisionTreeNode>>
        get() = next.map { (key, value) -> Outcome(key, value.first) }.toSet()

    override val values
        get() = next.values.map { it.first }

    override fun isEmpty(): Boolean {
        return next.isEmpty()
    }

    override fun containsValue(value: DecisionTreeNode): Boolean {
        return next.values.any { it.first == value }
    }

    override fun containsKey(key: KeyType): Boolean {
        return next.containsKey(key)
    }

    override val keys
        get() = next.keys

    override val size: Int
        get() = next.size


}