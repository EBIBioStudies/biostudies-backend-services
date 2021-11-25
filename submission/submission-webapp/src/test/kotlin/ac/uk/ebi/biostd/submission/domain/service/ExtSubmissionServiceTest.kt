package ac.uk.ebi.biostd.submission.domain.service

import DefaultCollection.Companion.defaultCollection
import DefaultSubmission.Companion.defaultSubmission
import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.exception.ExtSubmissionMappingException
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.events.SubmissionRequestMessage
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.PROJECT_TYPE
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
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
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.SUBMISSIONS_REQUEST_ROUTING_KEY
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class ExtSubmissionServiceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val rabbitTemplate: RabbitTemplate,
    @MockK private val requestService: SubmissionRequestService,
    @MockK private val submissionRepository: SubmissionQueryService,
    @MockK private val userPrivilegesService: IUserPrivilegesService,
    @MockK private val securityQueryService: ISecurityQueryService,
    @MockK private val extSerializationService: ExtSerializationService
) {
    private val extSubmission = defaultSubmission(collections = listOf(defaultCollection("ArrayExpress")))
    private val testInstance =
        ExtSubmissionService(
            rabbitTemplate,
            requestService,
            submissionRepository,
            userPrivilegesService,
            securityQueryService,
            extSerializationService
        )

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
        assertThat(submissionFilter.released).isTrue
        assertThat(submissionFilter.rTimeTo).isEqualTo("2020-09-21T15:00:00Z")
        assertThat(submissionFilter.rTimeFrom).isEqualTo("2019-09-21T15:00:00Z")
        verify(exactly = 1) { submissionRepository.getExtendedSubmissions(submissionFilter) }
    }

    @Test
    fun `submit extended`() {
        val saveRequest = slot<SaveSubmissionRequest>()
        every { requestService.saveAndProcessSubmissionRequest(capture(saveRequest)) } returns extSubmission

        testInstance.submitExt("user@mail.com", extSubmission)

        val request = saveRequest.captured
        assertThat(request.fileMode).isEqualTo(COPY)
        assertThat(request.submission).isEqualTo(extSubmission.copy(submitter = "user@mail.com"))
        verify(exactly = 1) {
            submissionRepository.existByAccNo("ArrayExpress")
            securityQueryService.existsByEmail("owner@email.org")
            requestService.saveAndProcessSubmissionRequest(request)
        }
    }

    @Test
    fun `submit extended async`() {
        val saveRequest = slot<SaveSubmissionRequest>()
        val requestMessage = slot<SubmissionRequestMessage>()

        every { requestService.saveSubmissionRequest(capture(saveRequest)) } returns extSubmission.copy(version = 2)
        every {
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_REQUEST_ROUTING_KEY, capture(requestMessage))
        } answers { nothing }

        testInstance.submitExtAsync("user@mail.com", extSubmission)

        val request = saveRequest.captured
        assertThat(request.fileMode).isEqualTo(COPY)
        assertThat(request.submission).isEqualTo(extSubmission.copy(submitter = "user@mail.com"))

        val asyncMessage = requestMessage.captured
        assertThat(asyncMessage.draftKey).isNull()
        assertThat(asyncMessage.version).isEqualTo(2)
        assertThat(asyncMessage.fileMode).isEqualTo(COPY)
        assertThat(asyncMessage.accNo).isEqualTo(extSubmission.accNo)

        verify(exactly = 1) {
            requestService.saveSubmissionRequest(request)
            submissionRepository.existByAccNo("ArrayExpress")
            securityQueryService.existsByEmail("owner@email.org")
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_REQUEST_ROUTING_KEY, asyncMessage)
        }
    }

    @Test
    fun `submit extended with file lists`(
        @MockK extFile: ExtFile
    ) {
        val saveRequest = slot<SaveSubmissionRequest>()
        val fileList = tempFolder.createFile("file-list.json", "referenced")
        val populatedFileList = ExtFileList("file-list", listOf(extFile))
        val submission = extSubmission.copy(section = ExtSection(type = "Study", fileList = ExtFileList("file-list")))

        every { requestService.saveAndProcessSubmissionRequest(capture(saveRequest)) } returns submission
        every {
            extSerializationService.deserialize("referenced", ExtFileTable::class.java)
        } returns ExtFileTable(extFile)

        testInstance.submitExt("user@mail.com", submission, listOf(fileList))

        assertThat(saveRequest.captured.fileMode).isEqualTo(COPY)
        assertThat(saveRequest.captured.submission).isEqualToComparingFieldByField(
            submission.copy(
                submitter = "user@mail.com",
                section = ExtSection(type = "Study", fileList = populatedFileList)
            )
        )
        verify(exactly = 1) {
            submissionRepository.existByAccNo("ArrayExpress")
            securityQueryService.existsByEmail("owner@email.org")
            extSerializationService.deserialize("referenced", ExtFileTable::class.java)
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
        every { securityQueryService.existsByEmail("owner@email.org") } returns false

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
        val saveRequest = slot<SaveSubmissionRequest>()
        val collection = extSubmission.copy(section = ExtSection(type = PROJECT_TYPE))

        every { submissionRepository.existByAccNo("ArrayExpress") } returns false
        every { requestService.saveAndProcessSubmissionRequest(capture(saveRequest)) } returns collection

        testInstance.submitExt("user@mail.com", collection)

        assertThat(saveRequest.captured.fileMode).isEqualTo(COPY)
        assertThat(saveRequest.captured.submission).isEqualTo(collection.copy(submitter = "user@mail.com"))

        verify(exactly = 0) { submissionRepository.existByAccNo("ArrayExpress") }
        verify(exactly = 1) { securityQueryService.existsByEmail("owner@email.org") }
    }

    @Test
    fun `get referenced files`(
        @MockK extFile: ExtFile
    ) {
        every { submissionRepository.getReferencedFiles("S-BSST1", "file-list") } returns listOf(extFile)

        assertThat(testInstance.getReferencedFiles("S-BSST1", "file-list").files).containsExactly(extFile)
    }
}
