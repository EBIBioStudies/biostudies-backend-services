package ac.uk.ebi.biostd.submission.domain.extended

import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.ConcurrentSubException
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.service.DoiService
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod.PAGE_TAB
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.exception.UnauthorizedOperation
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
class ExtSubmissionServiceTest(
    @param:MockK private val doiService: DoiService,
    @param:MockK private val toSubmissionMapper: ToSubmissionMapper,
    @param:MockK private val submissionSubmitter: ExtSubmissionSubmitter,
    @param:MockK private val persistenceService: SubmissionPersistenceService,
    @param:MockK private val queryService: SubmissionPersistenceQueryService,
    @param:MockK private val privilegesService: IUserPrivilegesService,
    @param:MockK private val securityService: SecurityQueryService,
    @param:MockK private val eventsPublisherService: EventsPublisherService,
    @param:MockK private val requestService: SubmissionRequestPersistenceService,
) {
    private val testInstance =
        ExtSubmissionService(
            doiService,
            toSubmissionMapper,
            submissionSubmitter,
            persistenceService,
            queryService,
            privilegesService,
            securityService,
            eventsPublisherService,
            requestService,
        )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `submission with processing request`() =
        runTest {
            val submission = extSubmission()

            coEvery { requestService.hasProcessingRequest(ACC_NO) } returns true

            assertThrows<ConcurrentSubException> {
                testInstance.submitExtAsync(USER, submission)
            }

            coVerify(exactly = 1) { requestService.hasProcessingRequest(ACC_NO) }
            coVerify(exactly = 0) {
                privilegesService.canSubmitExtended(any())
                submissionSubmitter.createRqt(any())
                eventsPublisherService.submissionRequest(any(), any())
            }
        }

    @Test
    fun `user without privileges to submit extended`() =
        runTest {
            val submission = extSubmission()

            coEvery { requestService.hasProcessingRequest(ACC_NO) } returns false
            coEvery { privilegesService.canSubmitExtended(USER) } returns false

            assertThrows<UnauthorizedOperation> {
                testInstance.submitExtAsync(USER, submission)
            }

            coVerify(exactly = 1) {
                requestService.hasProcessingRequest(ACC_NO)
                privilegesService.canSubmitExtended(USER)
            }
            coVerify(exactly = 0) {
                securityService.existsByEmail(any(), any())
                submissionSubmitter.createRqt(any())
                eventsPublisherService.submissionRequest(any(), any())
            }
        }

    @Test
    fun `submission owner does not exist`() =
        runTest {
            val submission = extSubmission()

            coEvery { requestService.hasProcessingRequest(ACC_NO) } returns false
            coEvery { privilegesService.canSubmitExtended(USER) } returns true
            coEvery { securityService.existsByEmail(OWNER, false) } returns false

            assertThrows<UserNotFoundException> {
                testInstance.submitExtAsync(USER, submission)
            }

            coVerify(exactly = 1) {
                requestService.hasProcessingRequest(ACC_NO)
                privilegesService.canSubmitExtended(USER)
                securityService.existsByEmail(OWNER, false)
            }
            coVerify(exactly = 0) {
                queryService.existByAccNo(any())
                submissionSubmitter.createRqt(any())
                eventsPublisherService.submissionRequest(any(), any())
            }
        }

    @Test
    fun `referenced collection does not exist`() =
        runTest {
            val collectionAccNo = "S-COLLECTION"
            val submission = extSubmission(collections = listOf(ExtCollection(collectionAccNo)))

            coEvery { requestService.hasProcessingRequest(ACC_NO) } returns false
            coEvery { privilegesService.canSubmitExtended(USER) } returns true
            coEvery { securityService.existsByEmail(OWNER, false) } returns true
            coEvery { queryService.existByAccNo(collectionAccNo) } returns false

            assertThrows<CollectionNotFoundException> {
                testInstance.submitExtAsync(USER, submission)
            }

            coVerify(exactly = 1) {
                requestService.hasProcessingRequest(ACC_NO)
                privilegesService.canSubmitExtended(USER)
                securityService.existsByEmail(OWNER, false)
                queryService.existByAccNo(collectionAccNo)
            }
            coVerify(exactly = 0) {
                submissionSubmitter.createRqt(any())
                eventsPublisherService.submissionRequest(any(), any())
            }
        }

    private fun extSubmission(collections: List<ExtCollection> = emptyList()): ExtSubmission =
        ExtSubmission(
            accNo = ACC_NO,
            version = 1,
            schemaVersion = "1.0",
            title = "Test Submission",
            doi = null,
            owner = OWNER,
            submitter = "submitter@email.org",
            method = PAGE_TAB,
            relPath = "S-TEST/123/$ACC_NO",
            rootPath = null,
            released = false,
            secretKey = "a-secret-key",
            releaseTime = TIME,
            submissionTime = TIME,
            modificationTime = TIME,
            creationTime = TIME,
            section = ExtSection(type = "Study"),
            collections = collections,
            storageMode = NFS,
        )

    private companion object {
        const val ACC_NO = "S-TEST123"
        const val USER = "user@email.org"
        const val OWNER = "owner@email.org"
        val TIME: OffsetDateTime = OffsetDateTime.of(2018, 9, 21, 0, 0, 0, 0, UTC)
    }
}
