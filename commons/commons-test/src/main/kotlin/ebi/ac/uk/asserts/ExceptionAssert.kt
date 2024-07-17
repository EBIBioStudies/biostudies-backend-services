package ebi.ac.uk.asserts

import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.Assertions

@Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException")
inline fun <reified T : Throwable> assertThrows(function: () -> Unit): T {
    try {
        function()
    } catch (e: Exception) {
        if (e is T) return e
    }
    throw AssertionError("Expected ${T::class.simpleName} to be thrown")
}

@Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException")
inline fun <reified T : Throwable> assertThatThrows(function: () -> Unit): AbstractThrowableAssert<*, T> {
    try {
        function()
    } catch (e: Exception) {
        if (e is T) return Assertions.assertThat(e)
    }
    throw AssertionError("Expected ${T::class.simpleName} to be thrown")
}
