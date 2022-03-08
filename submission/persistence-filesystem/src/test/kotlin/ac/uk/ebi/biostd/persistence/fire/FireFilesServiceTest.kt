package ac.uk.ebi.biostd.persistence.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.test.basicExtSubmission
import ebi.ac.uk.util.collections.ifLeft
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.FireFile as ClientFireFile

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FireFilesServiceTest(
    tempFolder: TemporaryFolder,
    @MockK private val fireWebClient: FireWebClient,
    @MockK private val submissionQueryService: SubmissionQueryService
) {
    private val file = tempFolder.createFile("test.txt")
    private val testMd5 = file.md5()
    private val attribute = ExtAttribute("Type", "Test")
    private val testInstance = FireFilesService(fireWebClient, submissionQueryService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { submissionQueryService.findLatestExtByAccNo(basicExtSubmission.accNo, true) } returns null
        every { fireWebClient.save(file, testMd5) } returns ClientFireFile(1, "abc1", testMd5, 1, "2021-07-08")
        every {
            fireWebClient.setPath("abc1", "${basicExtSubmission.relPath}/Files/folder/test.txt")
        } answers { nothing }
    }

    @Test
    fun `process submission with new file and previous version`() {
        val previousFile = FireFile("test.txt", "Files/test.txt", "dda2", "md5", 1L, listOf())
        val previousVersion = basicExtSubmission.copy(
            version = 1,
            section = ExtSection(type = "Study", files = listOf(left(previousFile)))
        )

        val nfsFile = createNfsFile("folder/test.txt", "Files/folder/test.txt", file, listOf(attribute))
        val section = ExtSection(type = "Study", files = listOf(left(nfsFile)))
        val submission = basicExtSubmission.copy(section = section)

        every { fireWebClient.unpublish("dda2") } answers { nothing }
        every { fireWebClient.unsetPath("dda2") } answers { nothing }
        every { submissionQueryService.findLatestExtByAccNo(basicExtSubmission.accNo, true) } returns previousVersion

        val processed = testInstance.persistSubmissionFiles(FilePersistenceRequest(submission))

        assertFireFile(processed, "test.txt", "folder/test.txt")
        verify(exactly = 1) {
            fireWebClient.unpublish("dda2")
            fireWebClient.unsetPath("dda2")
            fireWebClient.save(file, testMd5)
            fireWebClient.setPath("abc1", "S-TEST/123/S-TEST123/Files/folder/test.txt")
        }
    }

    @Test
    fun `process submission with existing file`() {
        val nfsFile = createNfsFile("folder/test.txt", "Files/folder/test.txt", file, listOf(attribute))
        val previousFile = FireFile("folder/test.txt", "Files/folder/test.txt", "abc1", testMd5, 1, listOf(attribute))
        val previousFiles = mapOf(Pair(testMd5, previousFile))
        val section = ExtSection(type = "Study", files = listOf(left(nfsFile)))
        val submission = basicExtSubmission.copy(section = section)
        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)

        val processed = testInstance.persistSubmissionFiles(request)

        assertFireFile(processed, "test.txt", "folder/test.txt")
        verify(exactly = 0) {
            fireWebClient.save(file, testMd5)
            fireWebClient.setPath("abc1", "S-TEST/123/S-TEST123/Files/folder/test.txt")
        }
    }

    @Test
    fun `process submission replacing files`() {
        val nfsFile = createNfsFile("folder/test.txt", "Files/folder/test.txt", file, listOf(attribute))
        val previousFile =
            FireFile("folder/test.txt", "Files/folder/test.txt", "abc1", "a-different-md5", 1, listOf(attribute))
        val previousFiles = mapOf(Pair("a-different-md5", previousFile))
        val section = ExtSection(type = "Study", files = listOf(left(nfsFile)))
        val submission = basicExtSubmission.copy(section = section)
        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)

        val processed = testInstance.persistSubmissionFiles(request)

        assertFireFile(processed, "test.txt", "folder/test.txt")
        verify(exactly = 1) {
            fireWebClient.save(file, testMd5)
            fireWebClient.setPath("abc1", "S-TEST/123/S-TEST123/Files/folder/test.txt")
        }
    }

    @Test
    fun `process submission with path changed`() {
        val nfsFile = createNfsFile("new-folder/test.txt", "Files/folder/test.txt", file, listOf(attribute))
        val previousFile =
            FireFile("old-folder/test.txt", "Files/folder/test.txt", "abc1", testMd5, 1, listOf(attribute))
        val previousFiles = mapOf(Pair(testMd5, previousFile))
        val section = ExtSection(type = "Study", files = listOf(left(nfsFile)))
        val submission = basicExtSubmission.copy(section = section)
        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)

        val processed = testInstance.persistSubmissionFiles(request)

        assertFireFile(processed, "test.txt", "new-folder/test.txt")
        verify(exactly = 0) {
            fireWebClient.save(file, testMd5)
            fireWebClient.setPath("abc1", "S-TEST/123/S-TEST123/Files/folder/test.txt")
        }
    }

    @Test
    fun `process submission when new file is FireFile`() {
        val fireFile = FireFile("new-folder/test.txt", "Files/folder/test.txt", "abc1", testMd5, 1, listOf(attribute))
        val previousFile =
            FireFile("old-folder/test.txt", "Files/folder/test.txt", "abc1", testMd5, 1, listOf(attribute))
        val previousFiles = mapOf(Pair(testMd5, previousFile))
        val section = ExtSection(type = "Study", files = listOf(left(fireFile)))
        val submission = basicExtSubmission.copy(section = section)
        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)

        assertThat(testInstance.persistSubmissionFiles(request)).isEqualTo(submission)
        verify(exactly = 0) {
            fireWebClient.save(file, testMd5)
            fireWebClient.setPath("abc1", "S-TEST/123/S-TEST123/Files/folder/test.txt")
        }
    }

//    @Test
//    fun `process submission when new file is a fire directory`() {
//        val fireDirectory =
//            FireDirectory("test.txt", "new-folder/test.txt", "Files/folder/test.txt", testMd5, 1, listOf(attribute))
//        val previousFile =
//            FireDirectory("test.txt", "old-folder/test.txt", "Files/folder/test.txt", testMd5, 1, listOf(attribute))
//        val previousFiles = mapOf(Pair(testMd5, previousFile))
//        val section = ExtSection(type = "Study", files = listOf(left(fireDirectory)))
//        val submission = basicExtSubmission.copy(section = section)
//        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)
//
//        assertThat(testInstance.persistSubmissionFiles(request)).isEqualTo(submission)
//        verify(exactly = 0) { fireWebClient.save(file, testMd5, "S-TEST/123/S-TEST123/Files/folder/test.txt") }
//    }

    private fun assertFireFile(processed: ExtSubmission, fileName: String, filePath: String) {
        assertThat(processed.section.files).hasSize(1)
        processed.section.files.first().ifLeft {
            it as FireFile
            assertThat(it.fileName).isEqualTo(fileName)
            assertThat(it.filePath).isEqualTo(filePath)
            assertThat(it.relPath).isEqualTo("Files/folder/test.txt")
            assertThat(it.fireId).isEqualTo("abc1")
            assertThat(it.md5).isEqualTo(testMd5)
            assertThat(it.size).isEqualTo(1)
            assertThat(it.attributes).containsExactly(attribute)
        }
    }
}
