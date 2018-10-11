package ebi.ac.uk.base

fun <T : Any?> T.or(other: T): T {
    return this ?: other
}