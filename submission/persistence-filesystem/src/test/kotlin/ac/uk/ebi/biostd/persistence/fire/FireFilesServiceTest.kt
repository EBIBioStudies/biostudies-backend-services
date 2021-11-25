package ac.uk.ebi.biostd.persistence.fire

import DefaultAttribute.Companion.defaultAttribute
import DefaultFireFile
import DefaultFireFile.Companion.defaultFireFile
import DefaultNfsFile.Companion.defaultNfsFile
import DefaultSection.Companion.defaultSection
import DefaultSubmission.Companion.defaultSubmission
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.io.ext.md5
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
    @MockK private val fireWebClient: FireWebClient
) {
    private val file = tempFolder.createFile("test.txt")
    private val testMd5 = file.md5()
    private val testInstance = FireFilesService(fireWebClient)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every {
            fireWebClient.save(file, testMd5, "S-TEST/123/S-TEST123/Files/folder/test.txt")
        } returns ClientFireFile(1, "abc1", testMd5, 1, "2021-07-08")
    }

    @Test
    fun `process submission with non existing file`() {
        val nfsFile = defaultNfsFile(
            filePath = "folder/test.txt", relPath = "Files/folder/test.txt", file = file,
            attributes = listOf(defaultAttribute())
        )
        val section = defaultSection(files = listOf(left(nfsFile)))
        val submission = defaultSubmission(section = section)

        val processed = testInstance.persistSubmissionFiles(FilePersistenceRequest(submission))

        assertFireFile(processed, "Files/folder/test.txt", "abc1")
        verify(exactly = 1) { fireWebClient.save(file, testMd5, "S-TEST/123/S-TEST123/Files/folder/test.txt") }
    }

    @Test
    fun `process submission with existing file`() {
        val nfsFile = defaultNfsFile(
            filePath = "folder/test.txt",
            relPath = "Files/folder/test.txt",
            file = file,
            attributes = listOf(defaultAttribute())
        )
        val previousFile = defaultFireFile(md5 = testMd5, attributes = emptyList())
        val previousFiles = mapOf(Pair(testMd5, previousFile))
        val section = defaultSection(files = listOf(left(nfsFile)))
        val submission = defaultSubmission(section = section)
        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)

        val processed = testInstance.persistSubmissionFiles(request)

        assertFireFile(processed, relPath = nfsFile.relPath, fireId = DefaultFireFile.FIRE_ID)
        verify(exactly = 0) { fireWebClient.save(file, testMd5, "S-TEST/123/S-TEST123/Files/folder/test.txt") }
    }

    @Test
    fun `process submission replacing files`() {
        val nfsFile = defaultNfsFile(
            filePath = "folder/test.txt",
            relPath = "Files/folder/test.txt",
            file = file,
            attributes = listOf(defaultAttribute())
        )
        val previousFile = defaultFireFile(md5 = "a-different-md5", attributes = emptyList())
        val previousFiles = mapOf(Pair("a-different-md5", previousFile))
        val section = defaultSection(files = listOf(left(nfsFile)))
        val submission = defaultSubmission(section = section)
        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)

        val processed = testInstance.persistSubmissionFiles(request)

        assertFireFile(processed, relPath = nfsFile.relPath, fireId = "abc1")
        verify(exactly = 1) { fireWebClient.save(file, testMd5, "S-TEST/123/S-TEST123/Files/folder/test.txt") }
    }

    @Test
    fun `process submission with path changed`() {
        val nfsFile = defaultNfsFile(
            filePath = "new-folder/test.txt",
            relPath = "Files/new-folder/test.txt",
            file = file,
            attributes = listOf(defaultAttribute())
        )
        val previousFile = defaultFireFile(md5 = testMd5, attributes = emptyList())
        val previousFiles = mapOf(Pair(testMd5, previousFile))
        val section = defaultSection(files = listOf(left(nfsFile)))
        val submission = defaultSubmission(section = section)
        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)

        val processed = testInstance.persistSubmissionFiles(request)

        assertFireFile(processed, nfsFile.relPath, previousFile.fireId)
        verify(exactly = 0) { fireWebClient.save(file, testMd5, "S-TEST/123/S-TEST123/Files/folder/test.txt") }
    }

    @Test
    fun `process submission when new file is FireFile`() {
        val fireFile = defaultFireFile(
            filePath = "new-folder/test.txt",
            relPath = "Files/new-folder/test.txt",
            md5 = testMd5,
            attributes = listOf(defaultAttribute())
        )
        val previousFile = defaultFireFile(md5 = testMd5, attributes = emptyList())
        val previousFiles = mapOf(Pair(testMd5, previousFile))
        val section = defaultSection(files = listOf(left(fireFile)))
        val submission = defaultSubmission(section = section)
        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)

        assertThat(testInstance.persistSubmissionFiles(request)).isEqualTo(submission)
        verify(exactly = 0) { fireWebClient.save(file, testMd5, "S-TEST/123/S-TEST123/Files/folder/test.txt") }
    }

//    @Test
//    fun `process submission when new file is a fire directory`() {
//        val fireDirectory =
//            FireDirectory("test.txt", "new-folder/test.txt", "Files/folder/test.txt", testMd5, 1, listOf(attribute))
//        val previousFile =
//            FireDirectory("test.txt", "old-folder/test.txt", "Files/folder/test.txt", testMd5, 1, listOf(attribute))
//        val previousFiles = mapOf(Pair(testMd5, previousFile))
//        val section = defaultSection( files = listOf(left(fireDirectory)))
//        val submission = defaultSubmission(section = section)
//        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)
//
//        assertThat(testInstance.persistSubmissionFiles(request)).isEqualTo(submission)
//        verify(exactly = 0) { fireWebClient.save(file, testMd5, "S-TEST/123/S-TEST123/Files/folder/test.txt") }
//    }

    private fun assertFireFile(processed: ExtSubmission, relPath: String, fireId: String) {
        assertThat(processed.section.files).hasSize(1)
        processed.section.files.first().ifLeft {
            it as FireFile
            assertThat(it.fileName).isEqualTo(relPath.substringAfterLast("/"))
            assertThat(it.filePath).isEqualTo(relPath.substringAfterLast("Files/"))
            assertThat(it.relPath).isEqualTo(relPath)
            assertThat(it.fireId).isEqualTo(fireId)
            assertThat(it.md5).isEqualTo(testMd5)
            assertThat(it.size).isEqualTo(DefaultFireFile.SIZE)
            assertThat(it.attributes).containsExactly(defaultAttribute())
        }
    }
}
