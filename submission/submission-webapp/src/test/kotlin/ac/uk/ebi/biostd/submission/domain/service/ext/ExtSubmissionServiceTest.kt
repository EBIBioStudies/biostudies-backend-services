package ac.uk.ebi.biostd.submission.domain.service.ext

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.submitter.ExtSubmissionSubmitter
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.PROJECT_TYPE
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.UnauthorizedOperation
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
import uk.ac.ebi.events.service.EventsPublisherService

@ExtendWith(MockKExtension::class)
class ExtSubmissionServiceTest(
    @MockK private val submissionSubmitter: ExtSubmissionSubmitter,
    @MockK private val submissionRepository: SubmissionPersistenceQueryService,
    @MockK private val userPrivilegesService: IUserPrivilegesService,
    @MockK private val securityQueryService: ISecurityQueryService,
    @MockK private val properties: ApplicationProperties,
    @MockK private val eventsPublisher: EventsPublisherService,
) {
    private val extSubmission = basicExtSubmission.copy(collections = listOf(ExtCollection("ArrayExpress")))
    private val testInstance =
        ExtSubmissionService(
            submissionSubmitter,
            submissionRepository,
            userPrivilegesService,
            securityQueryService,
            properties,
            eventsPublisher
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
        every { properties.persistence.enableFire } returns true
    }

    @Test
    fun `submit extended`() {
        val submitRequestSlot = slot<ExtSubmitRequest>()

        every { properties.persistence.enableFire } returns true
        every { submissionSubmitter.handleRequest(extSubmission.accNo, 1) } returns extSubmission
        every { submissionSubmitter.createRequest(capture(submitRequestSlot)) } returns (extSubmission.accNo to 1)

        testInstance.submitExt("user@mail.com", extSubmission)

        val submissionRequest = submitRequestSlot.captured
        assertThat(submissionRequest.submission.submitter).isEqualTo("user@mail.com")
        assertThat(submissionRequest.submission.storageMode).isEqualTo(FIRE)
        assertThat(submissionRequest.submission.modificationTime).isAfter(extSubmission.modificationTime)
        verify(exactly = 1) {
            submissionRepository.existByAccNo("ArrayExpress")
            submissionSubmitter.createRequest(submissionRequest)
            submissionSubmitter.handleRequest(extSubmission.accNo, 1)
            securityQueryService.existsByEmail("owner@email.org", false)
        }
    }

    @Test
    fun `submit extended async`() {
        val requestSlot = slot<ExtSubmitRequest>()

        every { properties.persistence.enableFire } returns true
        every { submissionSubmitter.handleRequest(extSubmission.accNo, 1) } returns extSubmission
        every { submissionSubmitter.createRequest(capture(requestSlot)) } returns (extSubmission.accNo to 1)
        every { eventsPublisher.submissionRequest(extSubmission.accNo, extSubmission.version) } answers { nothing }

        testInstance.submitExtAsync("user@mail.com", extSubmission)

        val submissionRequest = requestSlot.captured
        assertThat(submissionRequest.submission.submitter).isEqualTo("user@mail.com")
        assertThat(submissionRequest.submission.storageMode).isEqualTo(FIRE)
        assertThat(submissionRequest.submission.modificationTime).isAfter(extSubmission.modificationTime)

        verify(exactly = 0) { submissionSubmitter.handleRequest(any(), any()) }
        verify(exactly = 1) {
            submissionRepository.existByAccNo("ArrayExpress")
            securityQueryService.existsByEmail("owner@email.org", false)
            eventsPublisher.submissionRequest(extSubmission.accNo, extSubmission.version)
        }
    }

    @Test
    fun `submit extended with regular user`() {
        val exception = assertThrows<UnauthorizedOperation> {
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
        val requestSlot = slot<ExtSubmitRequest>()
        val collection = extSubmission.copy(section = ExtSection(type = PROJECT_TYPE))

        every { properties.persistence.enableFire } returns true
        every { submissionRepository.existByAccNo("ArrayExpress") } returns false
        every { submissionSubmitter.handleRequest(collection.accNo, 1) } returns collection
        every { submissionSubmitter.createRequest(capture(requestSlot)) } returns (collection.accNo to collection.version)

        testInstance.submitExt("user@mail.com", collection)

        val request = requestSlot.captured
        assertThat(request.submission.submitter).isEqualTo("user@mail.com")
        assertThat(request.submission.storageMode).isEqualTo(FIRE)
        assertThat(request.submission.modificationTime).isAfter(extSubmission.modificationTime)

        verify(exactly = 0) { submissionRepository.existByAccNo("ArrayExpress") }
        verify(exactly = 1) { securityQueryService.existsByEmail("owner@email.org", false) }
    }
}
