package ebi.ac.uk.util.collections

import ebi.ac.uk.base.Either
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EitherExtensionsTest {
    @Test
    fun addLeft() {
        val eitherList: MutableList<Either<String, String>> = mutableListOf()
        eitherList.addLeft("a")
        eitherList.first().ifLeft { assertThat(it).isEqualTo("a") }
    }

    @Test
    fun addRight() {
        val eitherList: MutableList<Either<String, String>> = mutableListOf()
        eitherList.addRight("b")
        eitherList.first().ifRight { assertThat(it).isEqualTo("b") }
    }

    @Test
    fun reduceLeft() {
        val list = listOf(Either.left(1), Either.right(5))

        val result = list.reduceLeft { it }

        assertThat(result).containsOnly(1)
    }

    @Test
    fun mapLeft() {
        val list = listOf(Either.left(3), Either.right(5))

        val result = list.mapLeft { it * 2 }

        assertThat(result).containsOnly(Either.left(6), Either.right(5))
    }
}
