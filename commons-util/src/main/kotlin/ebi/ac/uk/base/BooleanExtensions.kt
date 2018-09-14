package ebi.ac.uk.base

/**
 * Return nullable boolean value or false if it is null.
 *
 */
fun Boolean?.orFalse(): Boolean = this ?: false


/**
 * Execute lambda when given boolean is true.
 *
 */
inline fun Boolean.whenTrue(function: () -> Any) {
    if (this) {
        function.invoke()
    }
}
