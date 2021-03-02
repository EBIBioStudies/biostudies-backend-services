package ebi.ac.uk.util.arrow

import arrow.core.Either
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EitherExtensionsKtTest {

    @Test
    fun mapLeft() {
        val list = listOf(Either.left(1), Either.right(5))

        val result = list.mapLeft { it }

        assertThat(result).containsOnly(1)
    }
}
