package ebi.ac.uk.await

import org.awaitility.core.ConditionFactory

fun <T> ConditionFactory.untilNotNull(func: () -> T): T {
    var value: T? = null
    until {
        value = func()
        value != null
    }
    return value!!
}
