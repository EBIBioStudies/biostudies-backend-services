package ac.uk.ebi.biostd.persistence.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.test.basicExtSubmission as basicExtSub
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
    @MockK(relaxUnitFun = true) private val fireWebClient: FireWebClient,
    @MockK private val submissionQueryService: SubmissionQueryService
) {
    private val clientFireFile = ClientFireFile(1, "abc1", "md5", 1, "2021-09-21")
    private val fileSample = FireFile("test.txt", "folder/test.txt", "relPath", "abc", "md5", 1, listOf())

    private val fileFileList = fileSample.copy(fireId = "abc1")
    private val filePageTab = fileSample.copy(fireId = "abc2")
    private val file = fileSample.copy(fireId = "abc3")
    private val fileTable = fileSample.copy(fireId = "abc4")
    private val subFileFileList = fileSample.copy(fireId = "abc5")
    private val subFilePageTab = fileSample.copy(fireId = "abc6")
    private val subFile = fileSample.copy(fireId = "abc7")
    private val subFileTable = fileSample.copy(fireId = "abc8")
    private val section = ExtSection(
        type = "Study",
        fileList = ExtFileList("fileName1", files = listOf(fileFileList), pageTabFiles = listOf(filePageTab)),
        files = listOf(left(file), right(ExtFileTable(fileTable))),
        sections = listOf(
            left(
                ExtSection(
                    type = "Study",
                    fileList = ExtFileList(
                        "fileName2",
                        files = listOf(subFileFileList),
                        pageTabFiles = listOf(subFilePageTab)
                    ),
                    files = listOf(left(subFile), right(ExtFileTable(subFileTable)))
                )
            )
        )
    )
    private val extSub = basicExtSub.copy(section = section, pageTabFiles = listOf(fileSample.copy(fireId = "abc9")))
    private val testInstance = FireFtpService(fireWebClient, submissionQueryService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { fireWebClient.findAllInPath(extSub.relPath) } returns listOf(clientFireFile)
        every { submissionQueryService.getReferencedFiles(extSub.accNo, "fileName1") } returns listOf(fileFileList)
        every { submissionQueryService.getReferencedFiles(extSub.accNo, "fileName2") } returns listOf(subFileFileList)
    }

    @Test
    fun `process public submission`() {
        val submission = extSub.copy(released = true)
        testInstance.processSubmissionFiles(submission)

        verifyCleanFtpFolder(exactly = 1)
        verifyFtpPublish(exactly = 1)
    }

    @Test
    fun `process private submission`() {
        val submission = extSub.copy(released = false)
        testInstance.processSubmissionFiles(submission)

        verifyCleanFtpFolder(exactly = 1)
        verifyFtpPublish(exactly = 0)
    }

    @Test
    fun `create ftp folder`() {
        val submission = extSub.copy(released = true)

        every { submissionQueryService.getExtByAccNo(submission.accNo) } returns submission

        testInstance.generateFtpLinks(submission.accNo)

        verifyCleanFtpFolder(exactly = 1)
        verifyFtpPublish(exactly = 1)
    }

    private fun verifyCleanFtpFolder(exactly: Int) = verify(exactly = exactly) {
        fireWebClient.unpublish("abc1")
        fireWebClient.unsetPath("abc1")
    }

    private fun verifyFtpPublish(exactly: Int) = verify(exactly = exactly) {
        fireWebClient.publish("abc1")
        fireWebClient.setPath("abc1", "${extSub.relPath}/relPath")
        fireWebClient.publish("abc2")
        fireWebClient.setPath("abc2", "${extSub.relPath}/relPath")
        fireWebClient.publish("abc3")
        fireWebClient.setPath("abc3", "${extSub.relPath}/relPath")
        fireWebClient.publish("abc4")
        fireWebClient.setPath("abc4", "${extSub.relPath}/relPath")
        fireWebClient.publish("abc5")
        fireWebClient.setPath("abc5", "${extSub.relPath}/relPath")
        fireWebClient.publish("abc6")
        fireWebClient.setPath("abc6", "${extSub.relPath}/relPath")
        fireWebClient.publish("abc7")
        fireWebClient.setPath("abc7", "${extSub.relPath}/relPath")
        fireWebClient.publish("abc8")
        fireWebClient.setPath("abc8", "${extSub.relPath}/relPath")
        fireWebClient.publish("abc9")
        fireWebClient.setPath("abc9", "${extSub.relPath}/relPath")
    }
}
