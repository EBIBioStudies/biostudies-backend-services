package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.submission.exceptions.InvalidPermissionsException
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.User
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@ExtendWith(MockKExtension::class)
class AccNoServiceTest(
    @MockK private val user: User,
    @MockK private val accNoPatternUtil: AccNoPatternUtil,
    @MockK private val persistenceContext: PersistenceContext,
    @MockK private val privilegesService: IUserPrivilegesService
) {
    private val testInstance = AccNoService(persistenceContext, accNoPatternUtil, privilegesService)
    private val testRequest = AccNoServiceRequest(user, "AAB12", "Study")

    @BeforeEach
    fun init() {
        every { user.email } returns "test@mail.com"
        every { persistenceContext.isNew("") } returns true
        every { accNoPatternUtil.isPattern(EMPTY) } returns false
        every { privilegesService.canProvideAccNo("test@mail.com") } returns true
        every { privilegesService.canResubmit("test@mail.com", "test@mail.com", emptyList()) } returns true
    }

    @ParameterizedTest(name = "prefix is {0} and numeric value is {1}")
    @CsvSource(
        "AA, 88, AA/AA0-99/AA88",
        "AA, 200, AA/AAxxx200/AA200"
    )
    fun submitUserCanSubmit(prefix: String, value: Long, expected: String) {
        assertThat(testInstance.getRelPath(AccNumber(prefix, value))).isEqualTo(expected)
    }

    @Test
    fun `no accession number, no parent accession`() {
        val testRequest = AccNoServiceRequest(user, EMPTY, "Study")

        every { accNoPatternUtil.getPattern(DEFAULT_PATTERN) } returns "S-BSST"
        every { persistenceContext.getSequenceNextValue("S-BSST") } returns 1L

        val accNo = testInstance.getAccNo(testRequest).toString()
        assertThat(accNo).isEqualTo("S-BSST1")
    }

    @Test
    @Disabled
    fun `no accession number but parent accession`() {
        val testRequest = AccNoServiceRequest(user, EMPTY, "Study", "P-ARENT", "!{P-ARENT,}")

        every { accNoPatternUtil.getPattern("!{P-ARENT,}") } returns "P-ARENT"
        every { persistenceContext.getSequenceNextValue("P-ARENT") } returns 1
        every { privilegesService.canResubmit("test@mail.com", "test@mail.com", emptyList()) } returns true
        every { persistenceContext.getAuthor(testRequest.accNo) } returns "test@mail.com"

        val accNo = testInstance.getAccNo(testRequest).toString()
        assertThat(accNo).isEqualTo("P-ARENT1")
    }

    @Test
    fun `accession number for a project`() {
        val testRequest = AccNoServiceRequest(user, "AProject", "Project")

        every { persistenceContext.isNew("AProject") } returns true

        val accNo = testInstance.getAccNo(testRequest).toString()
        assertThat(accNo).isEqualTo("AProject")
    }

    @Test
    fun `submission is new and user is not allowed provide accession number`() {
        every { persistenceContext.isNew("AAB12") } returns true
        every { privilegesService.canProvideAccNo("test@mail.com") } returns false

        assertThrows<InvalidPermissionsException> { testInstance.getAccNo(testRequest) }
    }

    @Test
    @Disabled
    fun `submission is not new and user is not allowed to update submission`() {
        every { persistenceContext.isNew("AAB12") } returns false
        every { privilegesService.canResubmit("test@mail.com", "test@mail.com", emptyList()) } returns false
        every { persistenceContext.getAuthor(testRequest.accNo) } returns "test@mail.com"

        assertThrows<InvalidPermissionsException> { testInstance.getAccNo(testRequest) }
    }
}
