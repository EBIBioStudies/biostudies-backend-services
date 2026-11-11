package uk.ac.ebi.biostd.asserts

import org.assertj.core.api.Assertions
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun assertThat(
    value: Boolean,
    message: () -> String,
) {
    contract {
        returns() implies value
    }
    Assertions.assertThat(value).isTrue().`as`(message())
}
