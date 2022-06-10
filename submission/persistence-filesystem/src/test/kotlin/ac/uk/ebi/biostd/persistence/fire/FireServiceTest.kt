package ac.uk.ebi.biostd.persistence.fire

import ac.uk.ebi.biostd.persistence.filesystem.fire.FireService
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.test.basicExtSubmission
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File
import java.util.zip.ZipFile

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class FireServiceTest(
    @MockK private val fireClient: FireClient,
    private val tempFolder: TemporaryFolder,
) {
    private val submission = basicExtSubmission
    private val testInstance = FireService(fireClient, tempFolder.createDirectory("fire-temp"))

    @Test
    fun `Get or persist when fire file`() {
        val fireFile = FireFile(
            filePath = "folder/test.txt",
            relPath = "Files/folder/test.txt",
            fireId = "abc1",
            md5 = "md5",
            size = 1,
            type = ExtFileType.FILE,
            attributes = listOf(ExtAttribute("Type", "Test"))
        )

        every { fireClient.setPath("abc1", "${submission.relPath}/Files/folder/test.txt") } answers { nothing }

        val response = testInstance.getOrPersist(submission, fireFile)

        assertThat(response).isEqualTo(fireFile)
    }

    @Nested
    inner class GetOrPersistNfsFileDirectory {
        private val fileSlot = slot<File>()
        private val md5Slot = slot<String>()
        private val folder = tempFolder.createDirectory("folder").apply { createFile("test.txt", "content") }
        private val nfsFile = createNfsFile("folder", "Files/folder", folder, listOf(ExtAttribute("Type", "Test")))
        private val fireFile = FireApiFile(1, "abc1", "folderMd5", 1, "2021-07-08")

        @Test
        fun `Get or persist when nfs folder`() {
            every { fireClient.save(capture(fileSlot), capture(md5Slot)) } returns fireFile
            every { fireClient.setPath("abc1", "${submission.relPath}/Files/folder.zip") } answers { nothing }
            every { fireClient.setBioMetadata("abc1", submission.accNo, "directory", false) } answers { nothing }
            every { fireClient.findByPath("S-TEST/123/S-TEST123/Files/folder") } returns null
            every { fireClient.findByMd5(folder.md5()) } returns emptyList()

            val folder = testInstance.getOrPersist(submission, nfsFile)

            assertThat(folder.fileName).isEqualTo("folder")
            assertThat(folder.filePath).isEqualTo("folder")
            assertThat(folder.relPath).isEqualTo("Files/folder")
            assertThat(folder.fireId).isEqualTo("abc1")
            assertThat(folder.md5).isEqualTo("folderMd5")
            assertThat(folder.size).isEqualTo(1)
            assertThat(folder.type).isEqualTo(ExtFileType.DIR)
            assertThat(folder.attributes).isEqualTo(nfsFile.attributes)

            val zipArtifact = ZipFile(fileSlot.captured)
            assertThat(zipArtifact.entries().nextElement().name).isEqualTo("test.txt")
        }
    }

    @Nested
    inner class GetOrPersistNfsFile {
        private val file = tempFolder.createFile("test.txt", "content")
        private val attribute = ExtAttribute("Type", "Test")
        private val nfsFile = createNfsFile("folder/test.txt", "Files/folder/test.txt", file, listOf(attribute))
        private val fireFile = FireApiFile(1, "abc1", file.md5(), 1, "2021-07-08")

        @Test
        fun `Get or persist when nfs file`() {
            every { fireClient.setPath("abc1", "${submission.relPath}/Files/folder/test.txt") } answers { nothing }
            every { fireClient.setBioMetadata("abc1", submission.accNo, "file", false) } answers { nothing }
            every { fireClient.findByPath("S-TEST/123/S-TEST123/Files/folder/test.txt") } returns null
            every { fireClient.findByMd5(nfsFile.md5) } returns emptyList()
            every { fireClient.save(file, file.md5()) } returns fireFile

            val response = testInstance.getOrPersist(submission, nfsFile)

            assertFireFile(response)
        }

        @Test
        fun `Get or persist when nfs file already persisted by path`() {
            every { fireClient.findByMd5(nfsFile.md5) } returns emptyList()
            every { fireClient.findByPath("S-TEST/123/S-TEST123/Files/folder/test.txt") } returns fireFile

            val response = testInstance.getOrPersist(submission, nfsFile)

            assertFireFile(response)
        }

        @Test
        fun `Get or persist when nfs file already persisted by md5`() {
            every { fireClient.findByMd5(nfsFile.md5) } returns listOf(fireFile)

            val response = testInstance.getOrPersist(submission, nfsFile)

            assertFireFile(response)
        }

        private fun assertFireFile(file: FireFile) {
            assertThat(file.fileName).isEqualTo("test.txt")
            assertThat(file.filePath).isEqualTo("folder/test.txt")
            assertThat(file.relPath).isEqualTo("Files/folder/test.txt")
            assertThat(file.fireId).isEqualTo("abc1")
            assertThat(file.md5).isEqualTo(this.file.md5())
            assertThat(file.size).isEqualTo(1)
            assertThat(file.type).isEqualTo(ExtFileType.FILE)
            assertThat(file.attributes).containsExactly(attribute)
        }
    }
}
