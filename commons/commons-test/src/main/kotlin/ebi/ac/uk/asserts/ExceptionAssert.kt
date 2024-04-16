package ebi.ac.uk.asserts

@Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException")
inline fun <reified T : Throwable> assertThrows(function: () -> Unit): T {
    try {
        function()
    } catch (e: Exception) {
        if (e is T) return e
    }
    throw AssertionError("Expected ${T::class.simpleName} to be thrown")
}
