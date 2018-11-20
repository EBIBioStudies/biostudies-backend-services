package ebi.ac.uk.arrow

import arrow.core.Option

inline fun <T> Option<T>.ifPresent(consumer: (T) -> Unit) = this.fold({}, consumer)
