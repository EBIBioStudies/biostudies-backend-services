package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.exceptions.InvalidPermissionsException
import arrow.core.Option
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@ExtendWith(MockKExtension::class)
class AccNoPatternUtilTest(
    @MockK private val user: User,
    @MockK private val context: PersistenceContext
) {

    private val submission: ExtendedSubmission = ExtendedSubmission("AAB12", user)

    private val testInstance = AccNoProcessor()

    @BeforeEach
    fun init() {
        every { context.canSubmit("", user) } returns true
        every { context.isNew(submission) } returns true
        every { context.canUserProvideAccNo(user) } returns true
        every { context.canSubmit("AAB12", user) } returns true
        every { context.getParentAccPattern(submission) } returns Option.empty()
    }

    @ParameterizedTest(name = "when prefix is {0}, postfix is {1} and numeric value is {2}")
    @CsvSource(
            "AA, BB, 88, AA/AA0-99BB/AA88BB",
            "AA, BB, 200, AA/AAxxx200BB/AA200BB",
            "AA, '', 88, AA/AA0-99/AA88",
            "AA, '', 200, AA/AAxxx200/AA200",
            "'', 'BB', 88, 0-99BB/88BB",
            "'', 'BB', 200, xxx200BB/200BB"
    )
    fun submitWhenUserCanSubmit(prefix: String, postfix: String, value: Long, expected: String) {
        assertThat(testInstance.getRelPath(AccNumber(AccPattern(prefix, postfix), value))).isEqualTo(expected)
    }

    @Test
    fun `When no accession number, no parent accession`() {
        every { context.getParentAccPattern(submission) } returns Option.empty()
        every { context.getSequenceNextValue(AccPattern("S-BSST")) } returns 1L

        submission.accNo = EMPTY

        testInstance.process(submission, context)
        assertThat(submission.accNo).isEqualTo("S-BSST1")
    }

    @Test
    fun `When no accession number but parent accession`() {
        every { context.getParentAccPattern(submission) } returns Option.just("!{P-ARENT,}")
        every { context.getSequenceNextValue(AccPattern("P-ARENT")) } returns 1
        submission.accNo = EMPTY

        testInstance.process(submission, context)
        assertThat(submission.accNo).isEqualTo("P-ARENT1")
    }

    @Test
    fun `When submission is new and user is not allow provide accession number`() {
        every { context.isNew(submission) } returns true
        every { context.canUserProvideAccNo(user) } returns false

        assertThrows<InvalidPermissionsException> { testInstance.process(submission, context) }
    }

    @Test
    fun `When accession and user is not allow to update submission`() {
        every { context.canSubmit("AAB12", user) } returns false

        assertThrows<InvalidPermissionsException> { testInstance.process(submission, context) }
    }
}
