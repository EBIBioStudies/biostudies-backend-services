package ebi.ac.uk.await

import org.awaitility.core.ConditionFactory

/**
 * Waits until the given function evaluate to not not and return value.
 * @param func function to evaluate.
 */
fun <T : Any> ConditionFactory.untilNotNull(func: () -> T): T {
    var value: T? = null
    until {
        value = func()
        value != null
    }
    return value!!
}
