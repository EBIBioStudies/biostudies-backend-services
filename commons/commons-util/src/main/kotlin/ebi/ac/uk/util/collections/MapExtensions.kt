package ebi.ac.uk.util.collections


fun <V, K> Map<Boolean, List<V>>.fold(whenTrue: (List<V>) -> K, whenFalse: (List<V>) -> K): List<K> {
    val result = mutableListOf<K>()
    result.add(whenTrue(this[false].orEmpty()))
    result.add(whenFalse(this[false].orEmpty()))
    return result
}

operator fun <V> Map<Boolean, List<V>>.component1() = this[true].orEmpty()
operator fun <V> Map<Boolean, List<V>>.component2() = this[false].orEmpty()
