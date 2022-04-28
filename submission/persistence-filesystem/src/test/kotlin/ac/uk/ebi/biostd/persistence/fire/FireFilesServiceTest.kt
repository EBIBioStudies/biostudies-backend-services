package ac.uk.ebi.biostd.persistence.fire

import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.test.basicExtSubmission
import ebi.ac.uk.util.collections.ifLeft
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.FireApiFile
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File
import kotlin.io.path.exists

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FireFilesServiceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val fireWebClient: FireWebClient
) {
    private val fireTempFolder = tempFolder.root.createDirectory("tmp").toPath()
    private val file = tempFolder.createFile("test.txt")
    private val testMd5 = file.md5()
    private val attribute = ExtAttribute("Type", "Test")
    private val processingService =
        FileProcessingService(ExtSerializationService(), FilesResolver(tempFolder.createDirectory("ext-files")))
    private val testInstance = FireFilesService(fireTempFolder, fireWebClient, processingService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        val fireFile = FireApiFile(1, "abc1", testMd5, 1, "2021-07-08")
        every { fireWebClient.save(file, testMd5) } returns fireFile
        every { fireWebClient.unsetPath("abc1") } answers { nothing }
        every { fireWebClient.findByAccNo(basicExtSubmission.accNo) } returns listOf(fireFile)
        every { fireWebClient.setBioMetadata("abc1", basicExtSubmission.accNo, "file", false) } answers { nothing }
        every {
            fireWebClient.setPath("abc1", "${basicExtSubmission.relPath}/Files/folder/test.txt")
        } answers { nothing }
    }

    @Test
    fun `process submission with nfs file and previous version`() {
        val nfsFile = createNfsFile("folder/test.txt", "Files/folder/test.txt", file, listOf(attribute))
        val section = ExtSection(type = "Study", files = listOf(left(nfsFile)))
        val submission = basicExtSubmission.copy(section = section)

        every { fireWebClient.unpublish("dda2") } answers { nothing }
        every { fireWebClient.unsetPath("dda2") } answers { nothing }
        every { fireWebClient.findByPath("S-TEST/123/S-TEST123/Files/folder/test.txt") } returns null

        val processed = testInstance.persistSubmissionFiles(FilePersistenceRequest(submission))

        assertFireFile(processed, "folder/test.txt")
        verify(exactly = 1) {
            fireWebClient.unsetPath("abc1")
            fireWebClient.save(file, testMd5)
            fireWebClient.setBioMetadata("abc1", basicExtSubmission.accNo, "file", false)
            fireWebClient.setPath("abc1", "S-TEST/123/S-TEST123/Files/folder/test.txt")
        }
    }

    @Test
    fun `process submission with path changed`() {
        val nfsFile = createNfsFile("new-folder/test.txt", "Files/folder/test.txt", file, listOf(attribute))
        val section = ExtSection(type = "Study", files = listOf(left(nfsFile)))
        val submission = basicExtSubmission.copy(section = section)
        val request = FilePersistenceRequest(submission)
        every { fireWebClient.findByPath("S-TEST/123/S-TEST123/Files/folder/test.txt") } returns null

        val processed = testInstance.persistSubmissionFiles(request)

        assertFireFile(processed, "new-folder/test.txt")
        verify(exactly = 1) {
            fireWebClient.save(file, testMd5)
            fireWebClient.setBioMetadata("abc1", basicExtSubmission.accNo, "file", false)
            fireWebClient.setPath("abc1", "S-TEST/123/S-TEST123/Files/folder/test.txt")
        }
    }

    @Test
    fun `process submission when new file is nfs directory`() {
        val fileSlot = slot<File>()
        val md5Slot = slot<String>()
        val folder = tempFolder.createDirectory("folder")
        val fireFile = FireApiFile(1, "abc1", "folderMd5", 1, "2021-07-08")

        folder.createNewFile("test.txt")
        every { fireWebClient.save(capture(fileSlot), capture(md5Slot)) } returns fireFile
        every { fireWebClient.findByPath("S-TEST/123/S-TEST123/Files/folder.zip") } returns null
        every { fireWebClient.setPath("abc1", "${basicExtSubmission.relPath}/Files/folder.zip") } answers { nothing }
        every { fireWebClient.setBioMetadata("abc1", basicExtSubmission.accNo, "directory", false) } answers { nothing }

        val nfsFile = createNfsFile("folder", "Files/folder", folder, listOf(attribute))
        val section = ExtSection(type = "Study", files = listOf(left(nfsFile)))
        val submission = basicExtSubmission.copy(section = section)
        val request = FilePersistenceRequest(submission)

        val processed = testInstance.persistSubmissionFiles(request)

        assertFireDirectory(processed)
        assertThat(fireTempFolder.resolve("S-TEST123/1/folder").exists()).isTrue
        verify(exactly = 1) {
            fireWebClient.save(fileSlot.captured, md5Slot.captured)
            fireWebClient.setPath("abc1", "S-TEST/123/S-TEST123/Files/folder.zip")
            fireWebClient.setBioMetadata("abc1", basicExtSubmission.accNo, "directory", false)
        }
    }

    @Test
    fun `process submission when new file is FireFile`() {
        val fireFile =
            FireFile("new-folder/test.txt", "Files/folder/test.txt", "abc1", testMd5, 1, FILE, listOf(attribute))
        val section = ExtSection(type = "Study", files = listOf(left(fireFile)))
        val submission = basicExtSubmission.copy(section = section)
        val request = FilePersistenceRequest(submission)

        assertThat(testInstance.persistSubmissionFiles(request)).isEqualTo(submission)
        verify(exactly = 0) {
            fireWebClient.save(file, testMd5)
            fireWebClient.setBioMetadata("abc1", basicExtSubmission.accNo, "file", false)
        }
        verify(exactly = 1) {
            fireWebClient.setPath("abc1", "S-TEST/123/S-TEST123/Files/folder/test.txt")
        }
    }

    @Test
    fun `process submission when new file is a fire directory`() {
        val fireDirectory = FireFile("folder.zip", "Files/folder.zip", "abc1", testMd5, 1, DIR, listOf(attribute))
        val section = ExtSection(type = "Study", files = listOf(left(fireDirectory)))
        val submission = basicExtSubmission.copy(section = section)
        val request = FilePersistenceRequest(submission)

        every { fireWebClient.setPath("abc1", "${basicExtSubmission.relPath}/Files/folder.zip") } answers { nothing }

        assertThat(testInstance.persistSubmissionFiles(request)).isEqualTo(submission)
    }

    private fun assertFireFile(processed: ExtSubmission, filePath: String) {
        assertThat(processed.section.files).hasSize(1)
        processed.section.files.first().ifLeft {
            it as FireFile
            assertThat(it.fileName).isEqualTo("test.txt")
            assertThat(it.filePath).isEqualTo(filePath)
            assertThat(it.relPath).isEqualTo("Files/folder/test.txt")
            assertThat(it.fireId).isEqualTo("abc1")
            assertThat(it.md5).isEqualTo(testMd5)
            assertThat(it.size).isEqualTo(1)
            assertThat(it.type).isEqualTo(FILE)
            assertThat(it.attributes).containsExactly(attribute)
        }
    }

    private fun assertFireDirectory(processed: ExtSubmission) {
        assertThat(processed.section.files).hasSize(1)
        processed.section.files.first().ifLeft {
            it as FireFile
            assertThat(it.fileName).isEqualTo("folder")
            assertThat(it.filePath).isEqualTo("folder")
            assertThat(it.relPath).isEqualTo("Files/folder")
            assertThat(it.fireId).isEqualTo("abc1")
            assertThat(it.md5).isEqualTo("folderMd5")
            assertThat(it.size).isEqualTo(1)
            assertThat(it.type).isEqualTo(DIR)
            assertThat(it.attributes).containsExactly(attribute)
        }
    }
}
