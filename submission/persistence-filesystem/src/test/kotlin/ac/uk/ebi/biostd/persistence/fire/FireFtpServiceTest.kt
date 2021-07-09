package ac.uk.ebi.biostd.persistence.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireWebClient

@ExtendWith(MockKExtension::class)
class FireFtpServiceTest(
    @MockK private val fireWebClient: FireWebClient,
    @MockK private val submissionQueryService: SubmissionQueryService
) {
    private val fireFile = FireFile("folder/test.txt", "abc1", "md5", 1, listOf())
    private val section = ExtSection(type = "Study", files = listOf(Either.left(fireFile)))
    private val testInstance = FireFtpService(fireWebClient, submissionQueryService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { fireWebClient.publish("abc1") } answers { nothing }
    }

    @Test
    fun `process public submission`() {
        val submission = basicExtSubmission.copy(released = true, section = section)
        testInstance.processSubmissionFiles(submission)

        verify(exactly = 1) { fireWebClient.publish("abc1") }
    }

    @Test
    fun `process private submission`() {
        val submission = basicExtSubmission.copy(released = false, section = section)
        testInstance.processSubmissionFiles(submission)

        verify(exactly = 0) { fireWebClient.publish("abc1") }
    }

    @Test
    fun `create ftp folder`() {
        val submission = basicExtSubmission.copy(released = true, section = section)

        every { submissionQueryService.getExtByAccNo(submission.accNo) } returns submission

        testInstance.generateFtpLinks(submission.accNo)

        verify(exactly = 1) { fireWebClient.publish("abc1") }
    }
}
