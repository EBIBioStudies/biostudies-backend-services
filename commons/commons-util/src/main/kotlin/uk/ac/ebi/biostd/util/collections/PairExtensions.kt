package uk.ac.ebi.biostd.util.collections

operator fun <T> Pair<T, *>?.component1() = this?.component1()

operator fun <T> Pair<*, T>?.component2() = this?.component2()
