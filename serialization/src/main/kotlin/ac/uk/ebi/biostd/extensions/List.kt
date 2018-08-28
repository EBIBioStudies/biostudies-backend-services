package ac.uk.ebi.biostd.extensions

fun <T> List<T>.second(): T {
    if (this.size < 2)
        throw NoSuchElementException("List do not contain a second element.")
    return this[1]
}
