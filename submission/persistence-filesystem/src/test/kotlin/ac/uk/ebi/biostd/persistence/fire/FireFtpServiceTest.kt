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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireWebClient

@Disabled("Update according to the current code")
@ExtendWith(MockKExtension::class)
class FireFtpServiceTest(
    @MockK(relaxUnitFun = true) private val fireWebClient: FireWebClient,
    @MockK private val submissionQueryService: SubmissionQueryService
) {
    private val fileFileList = fireFile(FILE_FILE_LIST)
    private val innerFileListFile = fireFile(INNER_FILE_FILE_LIST)
    private val extSub = createExtSubmission(fileFileList, innerFileListFile)
    private val testInstance = FireFtpService(fireWebClient, submissionQueryService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { submissionQueryService.getReferencedFiles(extSub.accNo, "fileName1") } returns listOf(fileFileList)
        every { submissionQueryService.getReferencedFiles(extSub.accNo, "fileName2") } returns listOf(innerFileListFile)
    }

    @Test
    fun `release submission files`() {
        val submission = extSub.copy(released = true)

        every { submissionQueryService.getExtByAccNo(submission.accNo, true) } returns submission

        testInstance.releaseSubmissionFiles(extSub.accNo, extSub.owner, extSub.relPath)

        verifyFtpPublish()
    }

    @Test
    fun `create ftp folder`() {
        val submission = extSub.copy(released = true)

        every { submissionQueryService.getExtByAccNo(submission.accNo, true) } returns submission

        testInstance.generateFtpLinks(submission.accNo)

        verifyFtpPublish()
    }

    private fun verifyFtpPublish() = verify(exactly = 1) {
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
        val filePageTab = fireFile(FILE_PAGE_TAB)
        val file = fireFile(FILE)
        val fileTable = fireFile(FILE_TABLE)
        val innerFileListPageTabFile = fireFile(INNER_FILE_PAGE_TAB)
        val innerFile = fireFile(INNER_FILE)
        val innerFileTable = fireFile(INNER_FILE_TABLE)
        val section = ExtSection(
            type = "Study",
            fileList = ExtFileList("fileName1", files = listOf(fileFileList), pageTabFiles = listOf(filePageTab)),
            files = listOf(left(file), right(ExtFileTable(fileTable))),
            sections = listOf(
                left(
                    ExtSection(
                        type = "Study",
                        fileList = ExtFileList(
                            "a/fileName2",
                            files = listOf(innerFileListFile),
                            pageTabFiles = listOf(innerFileListPageTabFile)
                        ),
                        files = listOf(left(innerFile), right(ExtFileTable(innerFileTable)))
                    )
                )
            )
        )
        return basicExtSub.copy(section = section, pageTabFiles = listOf(fireFile(fireId = SUB_FILE_PAGE_TAB)))
    }

    private fun fireFile(fireId: String) = FireFile("a/test.txt", "relPath", fireId, "md5", 1, listOf())

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
