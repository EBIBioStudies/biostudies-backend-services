package ac.uk.ebi.biostd.persistence.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.allFileList
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
import uk.ac.ebi.fire.client.model.FireFile as ClientFireFile

@ExtendWith(MockKExtension::class)
class FireFtpServiceTest(
    @MockK private val fireWebClient: FireWebClient,
    @MockK private val submissionQueryService: SubmissionQueryService
) {
    private val clientFireFile = ClientFireFile(1, "abc1", "md5", 1, "2021-09-21")
    private val fireFile = FireFile("test.txt", "folder/test.txt", "relPath", "abc1", "md5", 1, listOf())
    private val fireFileFileList = FireFile("test.txt", "folder/test.txt", "relPath", "abc2", "md5", 1, listOf())
    private val fireFilePageTab = FireFile("test.txt", "folder/test.txt", "relPath", "abc3", "md5", 1, listOf())
    private val section = ExtSection(
        type = "Study",
        fileList = ExtFileList(
            "fileList1",
            files = listOf(fireFileFileList),
            pageTabFiles = listOf(fireFilePageTab)
        ),
        files = listOf(Either.left(fireFile))
    )
    private val testInstance = FireFtpService(fireWebClient, submissionQueryService)

    @AfterEach
    fun afterEach() = clearAllMocks()


    fun createSubmission(): ExtSubmission {
        val subTabFile = FireFile("accno.json", "accno.json", "accno.json", "abc1", "md5", 1, listOf())
        val sectionFile = FireFile("test.txt", "folder/test.txt", "relPath", "abc1", "md5", 1, listOf())

        val fileListTabFile = FireFile("file-list.json", "file-list.json", "Files/file-list.json", "abc1", "md5", 1, listOf())
        val fileListFile = FireFile("test.txt", "folder/test.txt", "relPath", "abc1", "md5", 1, listOf())
        val fileList = ExtFileList("file-list", pageTabFiles = listOf(fileListTabFile))
        every { submissionQueryService }

        ExtSection(type = "Study", files = listOf(Either.left(sectionFile)), fileList = fileList)

        return basicExtSubmission.copy(
            released = true,
            section = section,
            pageTabFiles = listOf(tabFile))
    }

    @BeforeEach
    fun beforeEach() {


        every { fireWebClient.publish("abc1") } answers { nothing }
        every { fireWebClient.publish("abc2") } answers { nothing }
        every { fireWebClient.publish("abc3") } answers { nothing }
        every { fireWebClient.unpublish("abc1") } answers { nothing }
        every { fireWebClient.unsetPath("abc1") } answers { nothing }
        every { fireWebClient.findAllInPath(basicExtSubmission.relPath) } returns listOf(clientFireFile)
        every { fireWebClient.setPath("abc1", "${basicExtSubmission.relPath}/relPath") } answers { nothing }
        every { fireWebClient.setPath("abc2", "${basicExtSubmission.relPath}/relPath") } answers { nothing }
        every { fireWebClient.setPath("abc3", "${basicExtSubmission.relPath}/relPath") } answers { nothing }
        every { submissionQueryService.getReferencedFiles(basicExtSubmission.accNo, section.fileList!!.fileName) } returns listOf(
            fireFileFileList
        )
    }

    @Test
    fun `process public submission`() {
        val submission = basicExtSubmission.copy(released = true, section = section, pageTabFiles = listOf(tabFile))
        testInstance.processSubmissionFiles(submission)

        verifyCleanFtpFolder()
        verifyFtpPublish()
    }

    @Test
    fun `process private submission`() {
        val submission = basicExtSubmission.copy(released = false, section = section)
        testInstance.processSubmissionFiles(submission)

        verifyCleanFtpFolder()
        verify(exactly = 0) {
            fireWebClient.publish("abc1")
            fireWebClient.setPath("abc1", "${submission.relPath}/relPath")
        }
    }

    @Test
    fun `create ftp folder`() {
        val submission = basicExtSubmission.copy(released = true, section = section)

        every { submissionQueryService.getExtByAccNo(submission.accNo) } returns submission

        testInstance.generateFtpLinks(submission.accNo)

        verifyCleanFtpFolder()
        verifyFtpPublish()
    }

    private fun verifyCleanFtpFolder() = verify(exactly = 1) {
        fireWebClient.unpublish("abc1")
        fireWebClient.unsetPath("abc1")
    }

    private fun verifyFtpPublish() = verify(exactly = 1) {
        fireWebClient.publish("abc1")
        fireWebClient.setPath("abc1", "${basicExtSubmission.relPath}/relPath")
    }
}
