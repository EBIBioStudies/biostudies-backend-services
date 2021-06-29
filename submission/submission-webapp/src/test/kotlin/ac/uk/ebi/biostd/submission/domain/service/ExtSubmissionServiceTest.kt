package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.exception.ExtSubmissionMappingException
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.PROJECT_TYPE
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.test.basicExtSubmission
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
    @MockK private val requestService: SubmissionRequestService,
    @MockK private val submissionRepository: SubmissionQueryService,
    @MockK private val userPrivilegesService: IUserPrivilegesService,
    @MockK private val securityQueryService: ISecurityQueryService
) {
    private val extSubmission = basicExtSubmission.copy(collections = listOf(ExtCollection("ArrayExpress")))
    private val testInstance =
        ExtSubmissionService(requestService, submissionRepository, userPrivilegesService, securityQueryService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { submissionRepository.existByAccNo("ArrayExpress") } returns true
        every { securityQueryService.existsByEmail("owner@email.org") } returns true
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
            limit = 2
        )

        val pageable = Pageable.unpaged()
        val result1 = Result.success(extSubmission)
        val result2 = Result.failure<ExtSubmission>(ExtSubmissionMappingException("S-TEST123", "error"))
        val results = mutableListOf(result1, result2)
        val page = PageImpl(results, pageable, 2L)

        every { submissionRepository.getExtendedSubmissions(capture(filter)) } returns page

        val result = testInstance.getExtendedSubmissions(request)
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first()).isEqualTo(extSubmission)
        assertThat(result.pageable).isEqualTo(pageable)
        assertThat(result.totalElements).isEqualTo(2L)

        val submissionFilter = filter.captured
        assertThat(submissionFilter.released).isTrue()
        assertThat(submissionFilter.rTimeTo).isEqualTo("2020-09-21T15:00:00Z")
        assertThat(submissionFilter.rTimeFrom).isEqualTo("2019-09-21T15:00:00Z")
        verify(exactly = 1) { submissionRepository.getExtendedSubmissions(submissionFilter) }
    }

    @Test
    fun `submit extended`() {
        val saveRequest = slot<SaveSubmissionRequest>()
        every { requestService.saveAndProcessSubmissionRequest(capture(saveRequest)) } returns extSubmission

        testInstance.submitExtendedSubmission("user@mail.com", extSubmission)

        assertThat(saveRequest.captured.fileMode).isEqualTo(COPY)
        assertThat(saveRequest.captured.submission).isEqualTo(extSubmission.copy(submitter = "user@mail.com"))
        verify(exactly = 1) {
            submissionRepository.existByAccNo("ArrayExpress")
            securityQueryService.existsByEmail("owner@email.org")
        }
    }

    @Test
    fun `submit extended with regular user`() {
        val exception = assertThrows<SecurityException> {
            testInstance.submitExtendedSubmission("regular@mail.com", extSubmission)
        }

        assertThat(exception.message).isEqualTo("The user 'regular@mail.com' is not allowed to perform this action")
    }

    @Test
    fun `submit extended with non existing owner`() {
        every { securityQueryService.existsByEmail("owner@email.org") } returns false

        val exception = assertThrows<UserNotFoundException> {
            testInstance.submitExtendedSubmission("user@mail.com", extSubmission)
        }

        assertThat(exception.message).isEqualTo("The user with email 'owner@email.org' could not be found")
    }

    @Test
    fun `submit extended with non existing collection`() {
        every { submissionRepository.existByAccNo("ArrayExpress") } returns false

        val exception = assertThrows<CollectionNotFoundException> {
            testInstance.submitExtendedSubmission("user@mail.com", extSubmission)
        }

        assertThat(exception.message).isEqualTo("The collection 'ArrayExpress' was not found")
    }

    @Test
    fun `submit extended collection`() {
        val saveRequest = slot<SaveSubmissionRequest>()
        val collection = extSubmission.copy(section = ExtSection(type = PROJECT_TYPE))

        every { submissionRepository.existByAccNo("ArrayExpress") } returns false
        every { requestService.saveAndProcessSubmissionRequest(capture(saveRequest)) } returns collection

        testInstance.submitExtendedSubmission("user@mail.com", collection)

        assertThat(saveRequest.captured.fileMode).isEqualTo(COPY)
        assertThat(saveRequest.captured.submission).isEqualTo(collection.copy(submitter = "user@mail.com"))

        verify(exactly = 0) { submissionRepository.existByAccNo("ArrayExpress") }
        verify(exactly = 1) { securityQueryService.existsByEmail("owner@email.org") }
    }
}
