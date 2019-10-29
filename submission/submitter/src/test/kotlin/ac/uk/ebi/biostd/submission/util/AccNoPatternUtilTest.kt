package ac.uk.ebi.biostd.submission.util

import ac.uk.ebi.biostd.submission.exceptions.InvalidAccNoPattern
import ac.uk.ebi.biostd.submission.exceptions.InvalidPatternException
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AccNoPatternUtilTest(@MockK private val context: PersistenceContext) {
    private val testInstance = AccNoPatternUtil()

    @BeforeEach
    fun init() {
        every { context.getSequenceNextValue(any()) } returns 10
    }

    @Test
    fun `get acc pattern prefix`() {
        val prefix = "!{S-ABC}"
        val pattern = testInstance.getPattern(prefix)

        assertThat(pattern).isEqualTo("S-ABC")
    }

    @Test
    fun `get acc pattern when invalid pattern`() {
        val prefix = "!A"
        val invalidPattern = assertThrows<InvalidPatternException> { testInstance.getPattern(prefix) }

        assertThat(invalidPattern.message).isEqualTo("Invalid accession !A, submission pattern in the form ([A-Z,-]*)")
    }

    @Test
    fun isPattern() {
        assertThat(testInstance.isPattern("!A")).isFalse()
    }

    @Test
    fun `extract accession number`() {
        val accNo = testInstance.toAccNumber("S-TEST123")

        assertThat(accNo.prefix).isEqualTo("S-TEST")
        assertThat(accNo.numericValue).isEqualTo(123L)
    }

    @Test
    fun `extract invalid pattern`() {
        val error = assertThrows<InvalidAccNoPattern> { testInstance.toAccNumber("!A") }

        assertThat(error.message).isEqualTo(
            "Invalid accession number '!A', number should match the pattern (\\D*)([0-9]+)(\\D*)")
    }
}
