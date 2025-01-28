package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotDeleteSubmission
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotDeleteSubmissions
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService

@ExtendWith(MockKExtension::class)
class SubmissionServiceTest(
    @MockK private val user: SecurityUser,
    @MockK private val request: SubmitRequest,
    @MockK private val submission: ExtSubmission,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val userPrivilegesService: IUserPrivilegesService,
    @MockK private val submissionSubmitter: SubmissionSubmitter,
    @MockK private val eventsPublisherService: EventsPublisherService,
    @MockK private val submissionPersistenceService: SubmissionPersistenceService,
    @MockK private val fileStorageService: FileStorageService,
) {
    private val testInstance =
        SubmissionService(
            queryService,
            userPrivilegesService,
            submissionSubmitter,
            eventsPublisherService,
            submissionPersistenceService,
            fileStorageService,
        )

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @BeforeEach
    fun beforeEach() {
        setUpSubmissions()
        every { user.email } returns "user@mail.org"
    }

    @Test
    fun `submit async`() =
        runTest {
            every { request.accNo } returns "S-TEST123"
            every { request.owner } returns "owner@mail.org"
            every { request.accNo } returns "TMP_123456"
            every { request.singleJobMode } returns false
            coEvery { submissionSubmitter.processRequestDraft(request) } returns basicExtSubmission
            coEvery { submissionSubmitter.handleRequestAsync("S-TEST123", 1) } returns Unit

            testInstance.submitAsync(request)

            coVerify(exactly = 1) {
                submissionSubmitter.processRequestDraft(request)
                submissionSubmitter.handleRequestAsync("S-TEST123", 1)
            }
        }

    @Test
    fun `delete submission`() =
        runTest {
            coEvery { userPrivilegesService.canDelete("user@mail.org", "S-BSST1") } returns true

            testInstance.deleteSubmission("S-BSST1", user)

            coVerify(exactly = 1) {
                fileStorageService.deleteSubmissionFiles(submission)
                submissionPersistenceService.expireSubmission("S-BSST1")
                eventsPublisherService.submissionsRefresh("S-BSST1", "user@mail.org")
            }
        }

    @Test
    fun `delete submission without permission`() =
        runTest {
            coEvery { userPrivilegesService.canDelete("user@mail.org", "S-BSST1") } returns false

            val exception = assertThrows<UserCanNotDeleteSubmission> { testInstance.deleteSubmission("S-BSST1", user) }

            assertThat(exception.message)
                .isEqualTo("The user user@mail.org is not allowed to delete the submission S-BSST1")
            coVerify(exactly = 0) {
                fileStorageService.deleteSubmissionFiles(any())
                submissionPersistenceService.expireSubmission(any())
                eventsPublisherService.submissionsRefresh(any(), any())
            }
        }

    @Test
    fun `delete submissions`() =
        runTest {
            coEvery { queryService.getExtByAccNo("S-BSST2", includeFileListFiles = true) } returns submission

            testInstance.deleteSubmissions(listOf("S-BSST1", "S-BSST2"), user)

            coVerify(exactly = 2) {
                fileStorageService.deleteSubmissionFiles(submission)
            }
            coVerify(exactly = 1) {
                submissionPersistenceService.expireSubmission("S-BSST1")
                eventsPublisherService.submissionsRefresh("S-BSST1", "user@mail.org")
            }
            coVerify(exactly = 1) {
                submissionPersistenceService.expireSubmission("S-BSST2")
                eventsPublisherService.submissionsRefresh("S-BSST2", "user@mail.org")
            }
        }

    @Test
    fun `delete submissions without permissions`() =
        runTest {
            coEvery { userPrivilegesService.canDelete("user@mail.org", "S-BSST1") } returns false
            coEvery { userPrivilegesService.canDelete("user@mail.org", "S-BSST2") } returns true
            coEvery { userPrivilegesService.canDelete("user@mail.org", "S-BSST3") } returns false

            val exception =
                assertThrows<UserCanNotDeleteSubmissions> {
                    testInstance.deleteSubmissions(listOf("S-BSST1", "S-BSST2", "S-BSST3"), user)
                }

            assertThat(exception.message)
                .isEqualTo("The user user@mail.org is not allowed to delete the submissions S-BSST1, S-BSST3")
            coVerify(exactly = 0) {
                fileStorageService.deleteSubmissionFiles(any())
                submissionPersistenceService.expireSubmission(any())
                eventsPublisherService.submissionsRefresh(any(), any())
            }
        }

    private fun setUpSubmissions() {
        coEvery { fileStorageService.deleteSubmissionFiles(submission) } answers { nothing }
        coEvery { submissionPersistenceService.expireSubmission("S-BSST1") } answers { nothing }
        every { eventsPublisherService.submissionsRefresh("S-BSST1", "user@mail.org") } answers { nothing }
        coEvery { queryService.getExtByAccNo("S-BSST1", includeFileListFiles = true) } returns submission

        coEvery { userPrivilegesService.canDelete("user@mail.org", "S-BSST1") } returns true
        coEvery { userPrivilegesService.canDelete("user@mail.org", "S-BSST2") } returns true
        coEvery { submissionPersistenceService.expireSubmission("S-BSST2") } answers { nothing }
        every { eventsPublisherService.submissionsRefresh("S-BSST2", "user@mail.org") } answers { nothing }
    }
}
