package ebi.ac.uk.io

inline fun <A : AutoCloseable, B : AutoCloseable> use(a: A, b: B, func: (A, B) -> Unit) {
    a.use { b.use { func(a, b) } }
}
