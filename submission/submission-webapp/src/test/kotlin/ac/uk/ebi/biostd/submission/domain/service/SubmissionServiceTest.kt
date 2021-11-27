package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.XmlFormat
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotDelete
import ac.uk.ebi.biostd.submission.ext.getSimpleByAccNo
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ebi.ac.uk.extended.events.FailedSubmissionRequestMessage
import ebi.ac.uk.extended.events.SubmissionRequestMessage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.model.Submission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.rabbit.core.RabbitTemplate
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.SUBMISSIONS_REQUEST_ROUTING_KEY
import uk.ac.ebi.events.service.EventsPublisherService

@ExtendWith(MockKExtension::class)
class SubmissionServiceTest(
    @MockK private val submissionQueryService: SubmissionQueryService,
    @MockK private val serializationService: SerializationService,
    @MockK private val userPrivilegesService: IUserPrivilegesService,
    @MockK private val submissionSubmitter: SubmissionSubmitter,
    @MockK private val eventsPublisherService: EventsPublisherService,
    @MockK private val rabbitTemplate: RabbitTemplate
) {
    private val testInstance = SubmissionService(
        submissionQueryService,
        serializationService,
        userPrivilegesService,
        submissionSubmitter,
        eventsPublisherService,
        rabbitTemplate
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun submit(
        @MockK extSubmission: ExtSubmission,
        @MockK submissionRequest: SubmissionRequest
    ) {
        every { submissionSubmitter.submit(submissionRequest) } returns extSubmission

        val submission = testInstance.submit(submissionRequest)

        assertThat(submission).isEqualTo(extSubmission)
        verify(exactly = 1) { submissionSubmitter.submit(submissionRequest) }
    }

    @Test
    fun submitAsync(
        @MockK extSubmission: ExtSubmission,
        @MockK submissionRequest: SubmissionRequest
    ) {
        val requestMessageSlot = slot<SubmissionRequestMessage>()
        val saveSubmissionRequest = SaveSubmissionRequest(extSubmission, COPY, "TMP_123456")

        every { extSubmission.version } returns 1
        every { extSubmission.accNo } returns "S-BSST1"
        every { submissionRequest.accNo } returns "S-BSST1"
        every { extSubmission.owner } returns "test@ebi.ac.uk"
        every { submissionSubmitter.submitAsync(submissionRequest) } returns saveSubmissionRequest
        every {
            rabbitTemplate.convertAndSend(
                BIOSTUDIES_EXCHANGE,
                SUBMISSIONS_REQUEST_ROUTING_KEY,
                capture(requestMessageSlot)
            )
        } answers { nothing }

        testInstance.submitAsync(submissionRequest)
        val requestMessage = requestMessageSlot.captured

        assertAsyncRequestMessage(requestMessage)
        verify(exactly = 1) {
            submissionSubmitter.submitAsync(submissionRequest)
            rabbitTemplate.convertAndSend(BIOSTUDIES_EXCHANGE, SUBMISSIONS_REQUEST_ROUTING_KEY, requestMessage)
        }
    }

    private fun assertAsyncRequestMessage(requestMessage: SubmissionRequestMessage) {
        assertThat(requestMessage.accNo).isEqualTo("S-BSST1")
        assertThat(requestMessage.version).isEqualTo(1)
        assertThat(requestMessage.fileMode).isEqualTo(COPY)
        assertThat(requestMessage.draftKey).isEqualTo("TMP_123456")
    }

    @Test
    fun `process submission`(
        @MockK extSubmission: ExtSubmission
    ) {
        val accNoSlot = slot<String>()
        val versionSlot = slot<Int>()
        val fileModeSlot = slot<FileMode>()
        val draftKeySlot = slot<String>()
        val message = SubmissionRequestMessage("S-BSST1", 1, COPY, "user@mail.org", "TMP_123")

        every { submissionQueryService.getRequest("S-BSST1", 1) } returns extSubmission
        every { eventsPublisherService.submissionSubmitted(extSubmission) } answers { nothing }
        every {
            submissionSubmitter.processRequest(
                capture(accNoSlot),
                capture(versionSlot),
                capture(fileModeSlot),
                capture(draftKeySlot)
            )
        } returns extSubmission

        testInstance.processSubmission(message)

        assertThat(accNoSlot.captured).isEqualTo("S-BSST1")
        assertThat(versionSlot.captured).isEqualTo(1)
        assertThat(fileModeSlot.captured).isEqualTo(COPY)
        assertThat(draftKeySlot.captured).isEqualTo("TMP_123")

        verify(exactly = 0) { eventsPublisherService.submissionFailed(any()) }
        verify(exactly = 1) {
            submissionSubmitter.processRequest(
                accNoSlot.captured,
                versionSlot.captured,
                fileModeSlot.captured,
                draftKeySlot.captured
            )
            eventsPublisherService.submissionSubmitted(extSubmission)
        }
    }

    @Test
    fun `process failed submission`(
        @MockK extSubmission: ExtSubmission
    ) {
        val accNoSlot = slot<String>()
        val versionSlot = slot<Int>()
        val fileModeSlot = slot<FileMode>()
        val draftKeySlot = slot<String>()
        val failedMessageSlot = slot<FailedSubmissionRequestMessage>()
        val message = SubmissionRequestMessage("S-BSST1", 1, COPY, "user@mail.org", "TMP_123")

        every { submissionQueryService.getRequest("S-BSST1", 1) } returns extSubmission
        every { eventsPublisherService.submissionFailed(capture(failedMessageSlot)) } answers { nothing }
        every {
            submissionSubmitter.processRequest(
                capture(accNoSlot),
                capture(versionSlot),
                capture(fileModeSlot),
                capture(draftKeySlot)
            )
        } throws Exception("an error message")

        testInstance.processSubmission(message)

        assertThat(accNoSlot.captured).isEqualTo("S-BSST1")
        assertThat(versionSlot.captured).isEqualTo(1)
        assertThat(fileModeSlot.captured).isEqualTo(COPY)
        assertThat(draftKeySlot.captured).isEqualTo("TMP_123")

        val failedMessage = failedMessageSlot.captured
        assertFailedMessage(failedMessage)

        verify(exactly = 0) { eventsPublisherService.submissionSubmitted(extSubmission) }
        verify(exactly = 1) {
            submissionSubmitter.processRequest(
                accNoSlot.captured,
                versionSlot.captured,
                fileModeSlot.captured,
                draftKeySlot.captured
            )
            eventsPublisherService.submissionFailed(failedMessage)
        }
    }

    private fun assertFailedMessage(failedMessage: FailedSubmissionRequestMessage) {
        assertThat(failedMessage.version).isEqualTo(1)
        assertThat(failedMessage.fileMode).isEqualTo(COPY)
        assertThat(failedMessage.accNo).isEqualTo("S-BSST1")
        assertThat(failedMessage.draftKey).isEqualTo("TMP_123")
        assertThat(failedMessage.errorMessage).isEqualTo("an error message")
    }

    @Test
    fun `get submission as json`() {
        mockkStatic("ebi.ac.uk.extended.mapping.to.ToSubmissionKt")
        val submission: Submission = mockk()
        every { submissionQueryService.getSimpleByAccNo("S-BSST1") } returns submission
        every { serializationService.serializeSubmission(submission, JsonPretty) } returns "{}"

        val result = testInstance.getSubmissionAsJson("S-BSST1")

        assertThat(result).isEqualTo("{}")
    }

    @Test
    fun `get submission as xml`() {
        mockkStatic("ebi.ac.uk.extended.mapping.to.ToSubmissionKt")
        val submission: Submission = mockk()
        every { submissionQueryService.getSimpleByAccNo("S-BSST1") } returns submission
        every { serializationService.serializeSubmission(submission, XmlFormat) } returns "{}"

        val result = testInstance.getSubmissionAsXml("S-BSST1")

        assertThat(result).isEqualTo("{}")
    }

    @Test
    fun `get submission as tsv`() {
        mockkStatic("ebi.ac.uk.extended.mapping.to.ToSubmissionKt")
        val submission: Submission = mockk()
        every { submissionQueryService.getSimpleByAccNo("S-BSST1") } returns submission
        every { serializationService.serializeSubmission(submission, SubFormat.TsvFormat.Tsv) } returns "{}"

        val result = testInstance.getSubmissionAsTsv("S-BSST1")

        assertThat(result).isEqualTo("{}")
    }

    @Test
    fun `get submissions`() {
        val user: SecurityUser = mockk()
        val filter: SubmissionFilter = mockk()
        val basicSubmission: BasicSubmission = mockk()
        every { user.email } returns "user@ebi.ac.uk"
        every { submissionQueryService.getSubmissionsByUser("user@ebi.ac.uk", filter) } returns listOf(basicSubmission)

        val result = testInstance.getSubmissions(user, filter)

        assertThat(result).isEqualTo(listOf(basicSubmission))
    }

    @Test
    fun `can delete submission`() {
        val user: SecurityUser = mockk()
        every { user.email } returns "user@ebi.ac.uk"
        every { userPrivilegesService.canDelete("user@ebi.ac.uk", "S-BSST1") } returns true
        every { submissionQueryService.expireSubmission("S-BSST1") } answers { nothing }

        testInstance.deleteSubmission("S-BSST1", user)

        verify(exactly = 1) { submissionQueryService.expireSubmission("S-BSST1") }
    }

    @Test
    fun `can not delete submission`() {
        val user: SecurityUser = mockk()
        every { user.email } returns "user@ebi.ac.uk"
        every { userPrivilegesService.canDelete("user@ebi.ac.uk", "S-BSST1") } returns false

        assertThatExceptionOfType(UserCanNotDelete::class.java)
            .isThrownBy { testInstance.deleteSubmission("S-BSST1", user) }
            .withMessage("The user {user@ebi.ac.uk} is not allowed to delete the submission S-BSST1")

        verify(exactly = 0) { submissionQueryService.expireSubmission("S-BSST1") }
    }

    @Test
    fun `can delete submissions`() {
        val user: SecurityUser = mockk()
        every { user.email } returns "user@ebi.ac.uk"
        every { userPrivilegesService.canDelete("user@ebi.ac.uk", "S-BSST1") } returns true
        every { userPrivilegesService.canDelete("user@ebi.ac.uk", "S-BSST2") } returns true
        every { submissionQueryService.expireSubmissions(listOf("S-BSST1", "S-BSST2")) } answers { nothing }

        testInstance.deleteSubmissions(listOf("S-BSST1", "S-BSST2"), user)

        verify(exactly = 1) { submissionQueryService.expireSubmissions(listOf("S-BSST1", "S-BSST2")) }
    }

    @Test
    fun `can not delete submissions`() {
        val user: SecurityUser = mockk()
        every { user.email } returns "user@ebi.ac.uk"
        every { userPrivilegesService.canDelete("user@ebi.ac.uk", "S-BSST1") } returns true
        every { userPrivilegesService.canDelete("user@ebi.ac.uk", "S-BSST2") } returns false
        every { submissionQueryService.expireSubmissions(listOf("S-BSST1", "S-BSST2")) } answers { nothing }


        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { testInstance.deleteSubmissions(listOf("S-BSST1", "S-BSST2"), user) }
            .withMessage("Failed requirement.")

        verify(exactly = 0) { submissionQueryService.expireSubmissions(listOf("S-BSST1", "S-BSST2")) }
    }

    @Test
    fun `get submission`() {
        val extSubmission: ExtSubmission = mockk()
        every { submissionQueryService.getExtByAccNo("S-BSST1") } returns extSubmission

        assertThat(testInstance.getSubmission("S-BSST1")).isEqualTo(extSubmission)
    }
}
