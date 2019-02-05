package ac.uk.ebi.biostd.submission.util

import ac.uk.ebi.biostd.submission.exceptions.InvalidPatternException
import ebi.ac.uk.model.AccPattern
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
class AccNoPatternUtilTest(
    @MockK private val context: PersistenceContext
) {

    private val testInstance = AccNoPatternUtil()

    @BeforeEach
    fun init() {
        every { context.getSequenceNextValue(any()) } returns 10
    }

    @Test
    fun `get acc pattern when only prefix`() {
        val prefix = "!{ABC,}"

        val pattern = testInstance.getPattern(prefix)
        assertThat(pattern).isInstanceOf(AccPattern::class.java)
        assertThat(pattern.prefix).isEqualTo("ABC")
        assertThat(pattern.postfix).isEmpty()
    }

    @Test
    fun `get acc pattern when only postfix`() {
        val prefix = "!{,ABC}"

        val pattern = testInstance.getPattern(prefix)
        assertThat(pattern).isInstanceOf(AccPattern::class.java)
        assertThat(pattern.postfix).isEqualTo("ABC")
        assertThat(pattern.prefix).isEmpty()
    }

    @Test
    fun `get acc pattern when prefix and postfix`() {
        val prefix = "!{A,Z}"

        val pattern = testInstance.getPattern(prefix)
        assertThat(pattern).isInstanceOf(AccPattern::class.java)
        assertThat(pattern.prefix).isEqualTo("A")
        assertThat(pattern.postfix).isEqualTo("Z")
    }

    @Test
    fun `get acc pattern when invalid pattern`() {
        val prefix = "!{A}"

        val invalidPattern = assertThrows<InvalidPatternException> { testInstance.getPattern(prefix) }
        assertThat(invalidPattern.message).isNotEmpty()
    }
}
