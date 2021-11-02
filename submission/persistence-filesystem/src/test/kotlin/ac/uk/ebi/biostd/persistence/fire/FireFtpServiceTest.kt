package ac.uk.ebi.biostd.persistence.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
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
    private val fileFileList = fileSample.copy(fireId = FILE_FILE_LIST)
    private val innerFileFileList = fileSample.copy(fireId = INNER_FILE_FILE_LIST)
    private val extSub = createExtSubmission(fileSample, fileFileList, innerFileFileList)
    private val testInstance = FireFtpService(fireWebClient, submissionQueryService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { fireWebClient.findAllInPath(extSub.relPath) } returns listOf(clientFireFile)
        every { submissionQueryService.getReferencedFiles(extSub.accNo, "fileName1") } returns listOf(fileFileList)
        every { submissionQueryService.getReferencedFiles(extSub.accNo, "fileName2") } returns listOf(innerFileFileList)
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
        fireWebClient.unpublish(FILE_FILE_LIST)
        fireWebClient.unsetPath(FILE_FILE_LIST)
    }

    private fun verifyFtpPublish(exactly: Int) = verify(exactly = exactly) {
        val relPath = "${extSub.relPath}/relPath"

        fireWebClient.publish(FILE_FILE_LIST)
        fireWebClient.setPath(FILE_FILE_LIST, relPath)
        fireWebClient.publish(FILE_PAGE_TAB)
        fireWebClient.setPath(FILE_PAGE_TAB, relPath)
        fireWebClient.publish(FILE)
        fireWebClient.setPath(FILE, relPath)
        fireWebClient.publish(FILE_TABLE)
        fireWebClient.setPath(FILE_TABLE, relPath)
        fireWebClient.publish(INNER_FILE_FILE_LIST)
        fireWebClient.setPath(INNER_FILE_FILE_LIST, relPath)
        fireWebClient.publish(INNER_FILE_PAGE_TAB)
        fireWebClient.setPath(INNER_FILE_PAGE_TAB, relPath)
        fireWebClient.publish(INNER_FILE)
        fireWebClient.setPath(INNER_FILE, relPath)
        fireWebClient.publish(INNER_FILE_TABLE)
        fireWebClient.setPath(INNER_FILE_TABLE, relPath)
        fireWebClient.publish(SUB_FILE_PAGE_TAB)
        fireWebClient.setPath(SUB_FILE_PAGE_TAB, relPath)
    }

    private fun createExtSubmission(
        fileSample: FireFile,
        fileFileList: FireFile,
        innerFileFileList: FireFile
    ): ExtSubmission {

        val filePageTab = fileSample.copy(fireId = FILE_PAGE_TAB)
        val file = fileSample.copy(fireId = FILE)
        val fileTable = fileSample.copy(fireId = FILE_TABLE)
        val innerFilePageTab = fileSample.copy(fireId = INNER_FILE_PAGE_TAB)
        val innerFile = fileSample.copy(fireId = INNER_FILE)
        val innerFileTable = fileSample.copy(fireId = INNER_FILE_TABLE)
        val section = ExtSection(
            type = "Study",
            fileList = ExtFileList("fileName1", files = listOf(fileFileList), pageTabFiles = listOf(filePageTab)),
            files = listOf(left(file), right(ExtFileTable(fileTable))),
            sections = listOf(
                left(
                    ExtSection(
                        type = "Study",
                        fileList = ExtFileList(
                            "fileName2",
                            files = listOf(innerFileFileList),
                            pageTabFiles = listOf(innerFilePageTab)
                        ),
                        files = listOf(left(innerFile), right(ExtFileTable(innerFileTable)))
                    )
                )
            )
        )
        return basicExtSub.copy(section = section, pageTabFiles = listOf(fileSample.copy(fireId = SUB_FILE_PAGE_TAB)))
    }

    companion object {
        const val FILE_FILE_LIST = "abc1"
        const val FILE_PAGE_TAB = "abc2"
        const val FILE = "abc3"
        const val FILE_TABLE = "abc4"
        const val INNER_FILE_FILE_LIST = "abc5"
        const val INNER_FILE_PAGE_TAB = "abc6"
        const val INNER_FILE = "abc7"
        const val INNER_FILE_TABLE = "abc8"
        const val SUB_FILE_PAGE_TAB = "abc9"
    }
}
