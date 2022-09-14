package ac.uk.ebi.biostd.persistence.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSection
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
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.createExtFileList
import uk.ac.ebi.fire.client.integration.web.FireClient
import ebi.ac.uk.test.basicExtSubmission as basicExtSub

@ExtendWith(MockKExtension::class)
class FireFtpServiceTest(
    @MockK(relaxUnitFun = true) private val fireClient: FireClient,
    @MockK private val queryService: SubmissionPersistenceQueryService,
) {
    private val fileFileList = fireFile(FILE_FILE_LIST)
    private val innerFileListFile = fireFile(INNER_FILE_FILE_LIST)
    private val extSub = createExtSubmission(fileFileList, innerFileListFile)
    private val testInstance = FireFtpService(fireClient, ExtSerializationService())

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { queryService.getReferencedFiles(extSub.accNo, "fileName1") } returns listOf(
            fileFileList
        )
        every { queryService.getReferencedFiles(extSub.accNo, "fileName2") } returns listOf(
            innerFileListFile
        )
    }

    @Test
    fun `release submission files`() {
        val submission = extSub.copy(released = true)

        testInstance.releaseSubmissionFiles(submission)

        verifyFtpPublish()
    }

    private fun verifyFtpPublish() = verify(exactly = 1) {
        fireClient.publish(FILE_FILE_LIST)
        fireClient.publish(FILE_PAGE_TAB)
        fireClient.publish(FIRE_FILE)
        fireClient.publish(FILE_TABLE)
        fireClient.publish(INNER_FILE_FILE_LIST)
        fireClient.publish(INNER_FILE_PAGE_TAB)
        fireClient.publish(INNER_FILE)
        fireClient.publish(INNER_FILE_TABLE)
        fireClient.publish(SUB_FILE_PAGE_TAB)
        fireClient.publish(FIRE_DIR)
    }

    private fun createExtSubmission(
        fileFileList: FireFile,
        innerFileListFile: FireFile,
    ): ExtSubmission {
        val filePageTab = fireFile(FILE_PAGE_TAB)
        val file = fireFile(FIRE_FILE)
        val fileTable = fireFile(FILE_TABLE)
        val innerFileListPageTabFile = fireFile(INNER_FILE_PAGE_TAB)
        val innerFile = fireFile(INNER_FILE)
        val innerFileTable = fireFile(INNER_FILE_TABLE)
        val section = ExtSection(
            type = "Study",
            fileList = ExtFileList(
                "fileName1",
                file = createExtFileList(fileFileList),
                pageTabFiles = listOf(filePageTab)
            ),
            files = listOf(left(file), left(fireDir()), right(ExtFileTable(fileTable))),
            sections = listOf(
                left(
                    ExtSection(
                        type = "Study",
                        fileList = ExtFileList(
                            "a/fileName2",
                            file = createExtFileList(innerFileListFile),
                            pageTabFiles = listOf(innerFileListPageTabFile)
                        ),
                        files = listOf(left(innerFile), right(ExtFileTable(innerFileTable)))
                    )
                )
            )
        )
        return basicExtSub.copy(section = section, pageTabFiles = listOf(fireFile(fireId = SUB_FILE_PAGE_TAB)))
    }

    private fun fireDir() = FireFile("folder", "Files/Folder", FIRE_DIR, "dir-md5", 2L, DIR, listOf())

    private fun fireFile(fireId: String) = FireFile("a/test.txt", "relPath", fireId, "md5", 1, FILE, listOf())

    private companion object {
        const val FILE_FILE_LIST = "abc1"
        const val FILE_PAGE_TAB = "abc2"
        const val FIRE_FILE = "abc3"
        const val FILE_TABLE = "abc4"
        const val INNER_FILE_FILE_LIST = "abc5"
        const val INNER_FILE_PAGE_TAB = "abc6"
        const val INNER_FILE = "abc7"
        const val INNER_FILE_TABLE = "abc8"
        const val SUB_FILE_PAGE_TAB = "abc9"
        const val FIRE_DIR = "abc10"
    }
}
