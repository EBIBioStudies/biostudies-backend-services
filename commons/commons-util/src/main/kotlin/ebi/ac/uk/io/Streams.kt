package ebi.ac.uk.io

/**
 * Allow to execute the given instances of autocloseable and guarantee their disposal after function execution.
 * Equivalent to kotlin @see [AutoCloseable.use].
 */
inline fun <A : AutoCloseable, B : AutoCloseable> use(a: A, b: B, func: (A, B) -> Unit) {
    a.use { b.use { func(a, b) } }
}

/**
 * Allow to execute the given instances of autocloseable and guarantee their disposal after function execution.
 * Equivalent to kotlin @see [AutoCloseable.use].
 */
suspend inline fun <A : AutoCloseable, B : AutoCloseable> useAsync(a: A, b: B, func: suspend (A, B) -> Unit) {
    a.use { b.use { func(a, b) } }
}
