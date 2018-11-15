package ebi.ac.uk.base

import arrow.core.Option
import arrow.core.Option.Companion.fromNullable
import java.util.Optional

fun <T : Any> Optional<T>.toOption(): Option<T> {
    return fromNullable(this.orElse(null))
}