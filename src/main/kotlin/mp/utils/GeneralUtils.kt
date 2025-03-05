package mp.utils

/**
 * Создает набор всех возможных комбинаций элементов из входных коллекций;
 *
 * Пример: если на входе [[a, b], [x, y]]
 * то на выходе [[a, x], [a, y], [b, x], [b, y]]
 */
fun <T> getCombinations(possibilities: List<Collection<T>>): List<MutableList<T>> {
    if (possibilities.isEmpty()) {
        return ArrayList()
    }
    val firstPossibility: Collection<T> = possibilities.first()
    if (possibilities.size == 1) {
        return firstPossibility.map { el -> mutableListOf(el) }
    }
    val otherPossibilities = possibilities.subList(1, possibilities.size)
    val otherPermutations = getCombinations(otherPossibilities)
    return firstPossibility.flatMap { el ->
        otherPermutations.map { otherPossibilitiesPerm ->
            mutableListOf(el).apply { addAll(otherPossibilitiesPerm) }
        }
    }
}
