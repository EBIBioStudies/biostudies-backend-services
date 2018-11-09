package ebi.ac.uk.util.collections

fun <T> List<T>.groupByCondition(compare: (T, T) -> Boolean): MutableList<MutableList<T>> {
    if (this.isEmpty()) {
        return mutableListOf()
    }

    var current = first()
    var currentList = mutableListOf(current)
    val result = mutableListOf(currentList)

    for (next in listIterator(1)) {
        if (compare(next, current)) {
            currentList.add(next)
        } else {
            currentList = mutableListOf(next)
            result += currentList
        }

        current = next
    }

    return result
}
