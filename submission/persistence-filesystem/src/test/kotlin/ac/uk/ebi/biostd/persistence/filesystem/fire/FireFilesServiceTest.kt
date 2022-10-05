package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FireFilePersistenceConfig
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.FireFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FileSystemEntry
import uk.ac.ebi.fire.client.model.FireApiFile

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class FireFilesServiceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val fireClient: FireClient,
    @MockK private val serializationService: ExtSerializationService,
) {
    private val fireTempDirPath = tempFolder.createDirectory("fire-temp")
    private val testInstance = FireFilesService(fireClient, fireTempDirPath, serializationService)

    @Test
    fun `persist fire file found by md5 and path`() {
        val file = fireFile()
        val path = FileSystemEntry("/S-BSST/001/S-BSST1/Files/folder/file.txt", published = true)
        val fireApiFile = fireApiFile().copy(filesystemEntry = path)

        every { fireClient.findByMd5("the-md5") } returns listOf(fireApiFile)

        val config = FireFilePersistenceConfig("S-BSST0", 1, "S-BSST/001/S-BSST1")
        val persisted = testInstance.persistSubmissionFile(file, config)

        assertThat(persisted).isEqualToComparingFieldByField(file)
        verify(exactly = 1) {
            fireClient.findByMd5("the-md5")
        }
        verify(exactly = 0) {
            fireClient.downloadByFireId("the-fire-id", "file.txt")
            fireClient.setPath("the-fire-id", "/S-BSST/001/S-BSST1/Files/folder/file.txt")
            fireClient.save(any(), "the-md5", 123L)
        }
    }

    @Test
    fun `persist fire file found by md5 but no path`() {
        val file = fireFile()
        val fireApiFile = fireApiFile()

        every { fireClient.findByMd5("the-md5") } returns listOf(fireApiFile)
        every { fireClient.setPath("the-fire-id", "/S-BSST/001/S-BSST1/Files/folder/file.txt") } answers { nothing }

        val config = FireFilePersistenceConfig("S-BSST0", 1, "S-BSST/001/S-BSST1")
        val persisted = testInstance.persistSubmissionFile(file, config)

        assertThat(persisted).isEqualToComparingFieldByField(file)
        verify(exactly = 1) {
            fireClient.findByMd5("the-md5")
            fireClient.setPath("the-fire-id", "/S-BSST/001/S-BSST1/Files/folder/file.txt")
        }
        verify(exactly = 0) {
            fireClient.downloadByFireId("the-fire-id", "file.txt")
            fireClient.save(any(), "the-md5", 123L)
        }
    }

    @Test
    fun `persist fire file found not found`() {
        val file = fireFile()
        val fireApiFile = fireApiFile()
        val content = tempFolder.createFile("file.txt")

        every { fireClient.findByMd5("the-md5") } returns listOf()
        every { fireClient.save(any(), "the-md5", 123L) } returns fireApiFile
        every { fireClient.downloadByFireId("the-fire-id", "file.txt") } returns content
        every { fireClient.setPath("the-fire-id", "/S-BSST/001/S-BSST1/Files/folder/file.txt") } answers { nothing }

        val config = FireFilePersistenceConfig("S-BSST0", 1, "S-BSST/001/S-BSST1")
        val persisted = testInstance.persistSubmissionFile(file, config)

        assertThat(persisted).isEqualToComparingFieldByField(file)
        verify(exactly = 1) {
            fireClient.findByMd5("the-md5")
            fireClient.save(any(), "the-md5", 123L)
            fireClient.downloadByFireId("the-fire-id", "file.txt")
            fireClient.setPath("the-fire-id", "/S-BSST/001/S-BSST1/Files/folder/file.txt")
        }
    }

    private fun fireFile() = FireFile(
        "folder/file.txt",
        "Files/folder/file.txt",
        "the-fire-id",
        "the-md5",
        123L,
        FILE,
        emptyList(),
    )

    private fun fireApiFile() = FireApiFile(
        456,
        "the-fire-id",
        "the-md5",
        123L,
        "2022-09-21"
    )
}
