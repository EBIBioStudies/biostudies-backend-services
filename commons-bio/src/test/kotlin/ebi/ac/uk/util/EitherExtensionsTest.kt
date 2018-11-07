package ebi.ac.uk.util

import arrow.core.Either
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EitherExtensionsTest {
    @Test
    fun addLeft() {
        val eitherList: MutableList<Either<String, String>> = mutableListOf()
        eitherList.addLeft("a")

        assertThat(eitherList[0].getLeft()).isEqualTo("a")
    }

    @Test
    fun addRight() {
        val eitherList: MutableList<Either<String, String>> = mutableListOf()
        eitherList.addRight("b")

        assertThat(eitherList[0].getRight()).isEqualTo("b")
    }
}
