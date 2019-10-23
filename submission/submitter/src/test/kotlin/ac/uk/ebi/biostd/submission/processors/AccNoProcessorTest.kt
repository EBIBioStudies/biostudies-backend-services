package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.exceptions.InvalidPermissionsException
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import arrow.core.Option
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
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
class AccNoProcessorTest(
    @MockK private val user: User,
    @MockK private val accNoPatternUtil: AccNoPatternUtil,
    @MockK private val persistenceContext: PersistenceContext,
    @MockK private val userPrivilegesService: IUserPrivilegesService
) {
    private val submission = ExtendedSubmission("AAB12", user)
    private val testInstance = AccNoProcessor(userPrivilegesService, accNoPatternUtil)

    @BeforeEach
    fun init() {
        every { user.email } returns "test@mail.com"
        every { persistenceContext.isNew("") } returns true
        every { persistenceContext.getParentAccPattern(submission) } returns Option.empty()
        every { accNoPatternUtil.isPattern(EMPTY) } returns false
        every { userPrivilegesService.canProvideAccNo("test@mail.com") } returns true
        every { userPrivilegesService.canResubmit("test@mail.com", user, null, emptyList()) } returns true
    }

    @ParameterizedTest(name = "prefix is {0}, postfix is {1} and numeric value is {2}")
    @CsvSource(
        "AA, BB, 88, AA/AA0-99BB/AA88BB",
        "AA, BB, 200, AA/AAxxx200BB/AA200BB",
        "AA, '', 88, AA/AA0-99/AA88",
        "AA, '', 200, AA/AAxxx200/AA200",
        "'', 'BB', 88, 0-99BB/88BB",
        "'', 'BB', 200, xxx200BB/200BB"
    )
    fun submitUserCanSubmit(prefix: String, postfix: String, value: Long, expected: String) {
        assertThat(testInstance.getRelPath(AccNumber(AccPattern(prefix, postfix), value))).isEqualTo(expected)
    }

    @Test
    fun `no accession number, no parent accession`() {
        val submission = ExtendedSubmission(EMPTY, user)

        every { accNoPatternUtil.getPattern(DEFAULT_PATTERN) } returns AccPattern("S-BSST")
        every { persistenceContext.getParentAccPattern(submission) } returns Option.empty()
        every { persistenceContext.getSequenceNextValue(AccPattern("S-BSST")) } returns 1L

        testInstance.process(submission, persistenceContext)
        assertThat(submission.accNo).isEqualTo("S-BSST1")
    }

    @Test
    fun `no accession number but parent accession`() {
        val submission = ExtendedSubmission(EMPTY, user)

        every { accNoPatternUtil.getPattern("!{P-ARENT,}") } returns AccPattern("P-ARENT")
        every { persistenceContext.getParentAccPattern(submission) } returns Option.just("!{P-ARENT,}")
        every { persistenceContext.getSequenceNextValue(AccPattern("P-ARENT")) } returns 1

        testInstance.process(submission, persistenceContext)
        assertThat(submission.accNo).isEqualTo("P-ARENT1")
    }

    @Test
    fun `submission is new and user is not allowed provide accession number`() {
        every { persistenceContext.isNew("AAB12") } returns true
        every { userPrivilegesService.canProvideAccNo("test@mail.com") } returns false

        assertThrows<InvalidPermissionsException> { testInstance.process(submission, persistenceContext) }
    }

    @Test
    fun `submission is not new and user is not allowed to update submission`() {
        every { persistenceContext.isNew("AAB12") } returns false
        every { userPrivilegesService.canResubmit("test@mail.com", user, null, emptyList()) } returns false

        assertThrows<InvalidPermissionsException> { testInstance.process(submission, persistenceContext) }
    }
}
