package ebi.ac.uk.base

import arrow.core.Option
import arrow.core.Option.Companion.fromNullable
import java.util.Optional

/**
 * Transform java @see [Optional] into arrow @see [Option] .
 */
fun <T : Any> Optional<T>.toOption(): Option<T> {
    return fromNullable(this.orElse(null))
}
