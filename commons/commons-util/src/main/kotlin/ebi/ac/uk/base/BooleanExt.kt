package ebi.ac.uk.base

/**
 * Return nullable boolean value or false if it is null.
 */
fun Boolean?.orFalse(): Boolean = this ?: false

/**
 * Execute lambda when given boolean is true.
 */
inline fun Boolean.ifTrue(function: () -> Unit) {
    if (this) {
        function.invoke()
    }
}

/**
 * Executes the given lambda based on the boolean value.
 *
 * @param ifTrue Function to be executed if the boolean is true
 * @param ifFalse Function to be executed if the boolean is false
 */
inline fun <T> Boolean.fold(ifTrue: () -> T, ifFalse: () -> T): T = if (this) ifTrue() else ifFalse()
