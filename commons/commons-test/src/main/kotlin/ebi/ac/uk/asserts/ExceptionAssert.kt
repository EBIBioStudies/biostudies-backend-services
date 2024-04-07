package ebi.ac.uk.asserts

inline suspend fun <reified T : Throwable> assertThrows(function: suspend () -> Unit): T {
    try {
        function()
    } catch (e: Exception) {
        if (e is T) return e
    }
    throw AssertionError("Expected ${T::class.simpleName} to be thrown")
}

