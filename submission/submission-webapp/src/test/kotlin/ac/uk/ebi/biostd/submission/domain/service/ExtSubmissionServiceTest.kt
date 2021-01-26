package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.exception.ExtSubmissionMappingException
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@ExtendWith(MockKExtension::class)
class ExtSubmissionServiceTest(
    @MockK private val extSubmission: ExtSubmission,
    @MockK private val requestService: SubmissionRequestService,
    @MockK private val submissionRepository: SubmissionQueryService,
    @MockK private val userPrivilegesService: IUserPrivilegesService
) {
    private val testInstance = ExtSubmissionService(requestService, submissionRepository, userPrivilegesService)

    @AfterEach
    fun afterEach() = clearAllMocks()

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
    fun `filtering extended submissions`(@MockK extSubmission: ExtSubmission) {
        val filter = slot<SubmissionFilter>()
        val request = ExtPageRequest(
            fromRTime = "2019-09-21T15:00:00Z",
            toRTime = "2020-09-21T15:00:00Z",
            released = true,
            offset = 1,
            limit = 2)

        val pageable = Pageable.unpaged()
        val result1 = Result.success(extSubmission)
        val result2 = Result.failure<ExtSubmission>(ExtSubmissionMappingException("S-TEST123", "error"))
        val results = mutableListOf(result1, result2)
        val page = PageImpl(results, pageable, 2L)

        every { submissionRepository.getExtendedSubmissions(capture(filter), 1, 2) } returns page

        val result = testInstance.getExtendedSubmissions(request)
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first()).isEqualTo(extSubmission)
        assertThat(result.pageable).isEqualTo(pageable)
        assertThat(result.totalElements).isEqualTo(1L)

        val submissionFilter = filter.captured
        assertThat(submissionFilter.released).isTrue()
        assertThat(submissionFilter.rTimeTo).isEqualTo("2020-09-21T15:00:00Z")
        assertThat(submissionFilter.rTimeFrom).isEqualTo("2019-09-21T15:00:00Z")
        verify(exactly = 1) { submissionRepository.getExtendedSubmissions(submissionFilter, 1, 2) }
    }

    @Test
    fun `submit extended`() {
        val saveRequest = slot<SaveSubmissionRequest>()
        every { requestService.saveAndProcessSubmissionRequest(capture(saveRequest)) } returns extSubmission

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
