package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.repositories.data.SubmissionRepository
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ExtSubmissionServiceTest(
    @MockK private val extSubmission: ExtSubmission,
    @MockK private val persistenceService: PersistenceService,
    @MockK private val submissionRepository: SubmissionRepository,
    @MockK private val userPrivilegesService: IUserPrivilegesService
) {
    private val testInstance = ExtSubmissionService(persistenceService, submissionRepository, userPrivilegesService)

    @BeforeEach
    fun beforeEach() {
        every { submissionRepository.getExtByAccNo("S-TEST123") } returns extSubmission
        every { userPrivilegesService.canSubmitExtended("user@mail.com") } returns true
        every { userPrivilegesService.canSubmitExtended("regular@mail.com") } returns false
    }

    @Test
    fun `get ext submission`() {
        val submission = testInstance.getExtendedSubmission("S-TEST123")
        assertThat(submission).isEqualTo(extSubmission)
    }

    @Test
    fun `submit extended`() {
        val saveRequest = slot<SaveSubmissionRequest>()
        every { persistenceService.saveAndProcessSubmissionRequest(capture(saveRequest)) } returns extSubmission

        val submitted = testInstance.submitExtendedSubmission("user@mail.com", extSubmission)
        assertThat(submitted).isEqualTo(extSubmission)
        assertThat(saveRequest.captured.fileMode).isEqualTo(COPY)
        assertThat(saveRequest.captured.submission).isEqualTo(extSubmission)
    }

    @Test
    fun `submit extended with regular user`() {
        val exception = assertThrows<SecurityException> {
            testInstance.submitExtendedSubmission("regular@mail.com", extSubmission)
        }

        assertThat(exception.message).isEqualTo("The user 'regular@mail.com' is not allowed to perform this action")
    }
}
