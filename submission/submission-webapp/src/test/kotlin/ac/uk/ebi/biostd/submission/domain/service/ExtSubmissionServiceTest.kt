package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtFile
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
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class)
class ExtSubmissionServiceTest(
    @MockK private val rabbitTemplate: RabbitTemplate,
    @MockK private val submissionSubmitter: SubmissionSubmitter,
    @MockK private val submissionRepository: SubmissionQueryService,
    @MockK private val userPrivilegesService: IUserPrivilegesService,
    @MockK private val securityQueryService: ISecurityQueryService,
    @MockK private val extSerializationService: ExtSerializationService,
    @MockK private val eventsPublisherService: EventsPublisherService
) {
    private val extSubmission = basicExtSubmission.copy(collections = listOf(ExtCollection("ArrayExpress")))
    private val testInstance =
        ExtSubmissionService(
            submissionSubmitter,
            submissionRepository,
            userPrivilegesService,
            securityQueryService,
            extSerializationService,
            eventsPublisherService
        )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { submissionRepository.existByAccNo("ArrayExpress") } returns true
        every { submissionRepository.getExtByAccNo("S-TEST123") } returns extSubmission
        every { userPrivilegesService.canSubmitExtended("user@mail.com") } returns true
        every { securityQueryService.existsByEmail("owner@email.org", false) } returns true
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
        val page = PageImpl(mutableListOf(extSubmission), pageable, 2L)

        every { submissionRepository.getExtendedSubmissions(capture(filter)) } returns page

        val result = testInstance.getExtendedSubmissions(request)
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first()).isEqualTo(extSubmission)
        assertThat(result.pageable).isEqualTo(pageable)
        assertThat(result.totalElements).isEqualTo(2L)

        val submissionFilter = filter.captured
        assertThat(submissionFilter.released).isTrue
        assertThat(submissionFilter.rTimeTo).isEqualTo("2020-09-21T15:00:00Z")
        assertThat(submissionFilter.rTimeFrom).isEqualTo("2019-09-21T15:00:00Z")
        verify(exactly = 1) { submissionRepository.getExtendedSubmissions(submissionFilter) }
    }

    @Test
    fun `submit extended`() {
        val submissionRequestSlot = slot<SubmissionRequest>()

        every { submissionSubmitter.processRequest(extSubmission.accNo, 1) } returns extSubmission
        every { submissionSubmitter.submitAsync(capture(submissionRequestSlot)) } returns (extSubmission.accNo to 1)

        testInstance.submitExt("user@mail.com", extSubmission)

        val submissionRequest = submissionRequestSlot.captured
        assertThat(submissionRequest.fileMode).isEqualTo(COPY)
        assertThat(submissionRequest.submission).isEqualTo(extSubmission.copy(submitter = "user@mail.com"))
        verify(exactly = 1) {
            submissionRepository.existByAccNo("ArrayExpress")
            submissionSubmitter.submitAsync(submissionRequest)
            submissionSubmitter.processRequest(extSubmission.accNo, 1)
            securityQueryService.existsByEmail("owner@email.org", false)
        }
    }

    @Test
    fun `submit extended async`() {
        val requestSlot = slot<SubmissionRequest>()

        every { submissionSubmitter.processRequest(extSubmission.accNo, 1) } returns extSubmission
        every { submissionSubmitter.submitAsync(capture(requestSlot)) } returns (extSubmission.accNo to 1)
        every {
            eventsPublisherService.submissionRequest(extSubmission.accNo, extSubmission.version)
        } answers { nothing }

        testInstance.submitExtAsync("user@mail.com", extSubmission, fileMode = COPY)

        val request = requestSlot.captured
        assertThat(request.fileMode).isEqualTo(COPY)
        assertThat(request.submission).isEqualTo(extSubmission.copy(submitter = "user@mail.com"))

        verify(exactly = 0) { submissionSubmitter.processRequest(any(), any()) }
        verify(exactly = 1) {
            submissionRepository.existByAccNo("ArrayExpress")
            securityQueryService.existsByEmail("owner@email.org", false)
            eventsPublisherService.submissionRequest(extSubmission.accNo, extSubmission.version)
        }
    }

    @Test
    fun `refresh submission`() {
        val submissionRequestSlot = slot<SubmissionRequest>()
        every { submissionRepository.getExtByAccNo("S-TEST123", true) } returns extSubmission
        every {
            eventsPublisherService.submissionsRefresh(extSubmission.accNo, extSubmission.owner)
        } answers { nothing }
        every { submissionSubmitter.processRequest(extSubmission.accNo, 1) } returns extSubmission
        every { submissionSubmitter.submitAsync(capture(submissionRequestSlot)) } returns (extSubmission.accNo to 1)

        testInstance.refreshSubmission(extSubmission.accNo, "user@mail.com")

        val submissionRequest = submissionRequestSlot.captured
        verify(exactly = 1) {
            submissionRepository.getExtByAccNo(extSubmission.accNo, true)
            submissionSubmitter.submitAsync(submissionRequest)
            submissionSubmitter.processRequest(extSubmission.accNo, 1)
            eventsPublisherService.submissionsRefresh(extSubmission.accNo, extSubmission.owner)
        }
    }

    @Test
    fun `submit extended with regular user`() {
        val exception = assertThrows<SecurityException> {
            testInstance.submitExt("regular@mail.com", extSubmission)
        }

        assertThat(exception.message).isEqualTo("The user 'regular@mail.com' is not allowed to perform this action")
    }

    @Test
    fun `submit extended with non existing owner`() {
        every { securityQueryService.existsByEmail("owner@email.org", false) } returns false

        val exception = assertThrows<UserNotFoundException> {
            testInstance.submitExt("user@mail.com", extSubmission)
        }

        assertThat(exception.message).isEqualTo("The user with email 'owner@email.org' could not be found")
    }

    @Test
    fun `submit extended with non existing collection`() {
        every { submissionRepository.existByAccNo("ArrayExpress") } returns false

        val exception = assertThrows<CollectionNotFoundException> {
            testInstance.submitExt("user@mail.com", extSubmission)
        }

        assertThat(exception.message).isEqualTo("The collection 'ArrayExpress' was not found")
    }

    @Test
    fun `submit extended collection`() {
        val saveRequest = slot<SubmissionRequest>()
        val collection = extSubmission.copy(section = ExtSection(type = PROJECT_TYPE))

        every { submissionRepository.existByAccNo("ArrayExpress") } returns false
        every { submissionSubmitter.processRequest(collection.accNo, 1) } returns collection
        every { submissionSubmitter.submitAsync(capture(saveRequest)) } returns (collection.accNo to collection.version)

        testInstance.submitExt("user@mail.com", collection)

        assertThat(saveRequest.captured.fileMode).isEqualTo(COPY)
        assertThat(saveRequest.captured.submission).isEqualTo(collection.copy(submitter = "user@mail.com"))

        verify(exactly = 0) { submissionRepository.existByAccNo("ArrayExpress") }
        verify(exactly = 1) { securityQueryService.existsByEmail("owner@email.org", false) }
    }

    @Test
    fun `get referenced files`(
        @MockK extFile: ExtFile
    ) {
        every { submissionRepository.getReferencedFiles("S-BSST1", "file-list") } returns listOf(extFile)

        assertThat(testInstance.getReferencedFiles("S-BSST1", "file-list").files).containsExactly(extFile)
    }
}
