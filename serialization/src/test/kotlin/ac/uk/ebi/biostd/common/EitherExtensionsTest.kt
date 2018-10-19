package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.serialization.common.addLeft
import ac.uk.ebi.biostd.serialization.common.addRight
import arrow.core.Either
import arrow.core.getOrElse
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

fun <A, B> Either<A, B>.getLeft(): A = swap().getOrElse { throw Exception("It's not a left element") }

fun <A, B> Either<A, B>.getRight(): B = getOrElse { throw Exception("It's not a right element") }
