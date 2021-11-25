package ac.uk.ebi.biostd.persistence.fire

import DefaultFileList.Companion.defaultFileList
import DefaultFileTable.Companion.defaultFileTable
import DefaultFireFile.Companion.defaultFireFile
import DefaultSection.Companion.defaultSection
import DefaultSubmission.Companion.defaultSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
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
    private val fileFileList = defaultFireFile(fireId = FILE_FILE_LIST)
    private val innerFileListFile = defaultFireFile(fireId = INNER_FILE_FILE_LIST)
    private val extSub = createExtSubmission(fileFileList, innerFileListFile)
    private val testInstance = FireFtpService(fireWebClient, submissionQueryService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { fireWebClient.findAllInPath(extSub.relPath) } returns listOf(clientFireFile)
        every { submissionQueryService.getReferencedFiles(extSub.accNo, "fileName1") } returns listOf(fileFileList)
        every { submissionQueryService.getReferencedFiles(extSub.accNo, "fileName2") } returns listOf(innerFileListFile)
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
        fun verifyPublishFile(fireId: String) {
            fireWebClient.publish(fireId)
            fireWebClient.setPath(fireId, relPath)
        }

        verifyPublishFile(FILE_FILE_LIST)
        verifyPublishFile(FILE_PAGE_TAB)
        verifyPublishFile(FILE)
        verifyPublishFile(FILE_TABLE)
        verifyPublishFile(INNER_FILE_FILE_LIST)
        verifyPublishFile(INNER_FILE_PAGE_TAB)
        verifyPublishFile(INNER_FILE)
        verifyPublishFile(INNER_FILE_TABLE)
        verifyPublishFile(SUB_FILE_PAGE_TAB)
    }

    private fun createExtSubmission(
        fileFileList: FireFile,
        innerFileListFile: FireFile
    ): ExtSubmission {

        val filePageTab = defaultFireFile(fireId = FILE_PAGE_TAB)
        val file = defaultFireFile(fireId = FILE)
        val fileTable = defaultFireFile(fireId = FILE_TABLE)
        val innerFileListPageTabFile = defaultFireFile(fireId = INNER_FILE_PAGE_TAB)
        val innerFile = defaultFireFile(fireId = INNER_FILE)
        val innerFileTable = defaultFireFile(fireId = INNER_FILE_TABLE)
        val section = defaultSection(
            fileList = defaultFileList("fileName1", files = listOf(fileFileList), pageTabFiles = listOf(filePageTab)),
            files = listOf(left(file), right(defaultFileTable(listOf(fileTable)))),
            sections = listOf(
                left(
                    defaultSection(
                        fileList = defaultFileList(
                            "fileName2",
                            files = listOf(innerFileListFile),
                            pageTabFiles = listOf(innerFileListPageTabFile)
                        ),
                        files = listOf(left(innerFile), right(ExtFileTable(innerFileTable)))
                    )
                )
            )
        )
        return defaultSubmission(section = section, pageTabFiles = listOf(defaultFireFile(fireId = SUB_FILE_PAGE_TAB)))
    }

    private companion object {
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
