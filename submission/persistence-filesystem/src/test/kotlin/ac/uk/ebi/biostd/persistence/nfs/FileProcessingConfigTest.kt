package ac.uk.ebi.biostd.persistence.nfs

import ac.uk.ebi.biostd.persistence.filesystem.nfs.nfsCopy
import ac.uk.ebi.biostd.persistence.filesystem.nfs.nfsMove
import ac.uk.ebi.biostd.persistence.filesystem.request.FileProcessingConfig
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.test.clean
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(TemporaryFolderExtension::class)
class FileProcessingConfigTest(private val tempFolder: TemporaryFolder) {
    @AfterEach
    fun afterEach() = tempFolder.clean()

    @Nested
    inner class Copy {
        @Test
        fun `copy already existing`() {
            val subFolder = tempFolder.createDirectory("S-BSST1").createDirectory("Files")
            val subFile = subFolder.createNewFile("test.txt")
            val currentFolder = tempFolder.createDirectory("S-BSST1_temp")
            currentFolder.createNewFile("test.txt")

            testCopy(subFile, subFolder, currentFolder)
        }

        @Test
        fun `replace existing`() {
            val subFolder = tempFolder.createDirectory("S-BSST1").createDirectory("Files")
            val subFile = subFolder.createNewFile("test.txt")
            val currentFolder = tempFolder.createDirectory("S-BSST1_temp")
            currentFolder.createNewFile("test.txt", "test-content")

            testCopy(subFile, subFolder, currentFolder)
        }

        @Test
        fun `copy new submission file`() {
            val subFolder = tempFolder.createDirectory("S-BSST1").createDirectory("Files")
            val userFolder = tempFolder.createDirectory("user-folder")
            val subFile = userFolder.createNewFile("test.txt")
            val currentFolder = tempFolder.createDirectory("S-BSST1_temp")

            testCopy(subFile, subFolder, currentFolder)
        }

        private fun testCopy(subFile: File, subFolder: File, currentFolder: File) {
            val extFile = NfsFile("test.txt", subFile)
            val config = FileProcessingConfig(subFolder, currentFolder, RW_R__R__, RWXR_XR_X)
            val result = config.nfsCopy(extFile)

            assertThat(subFolder.resolve("test.txt").exists()).isTrue()
            assertThat(result.file.path).isEqualTo("${tempFolder.root}/S-BSST1/Files/test.txt")
        }
    }

    @Nested
    inner class Move {
        @Test
        fun `move from temp folder`() {
            val subFolder = tempFolder.createDirectory("S-BSST1").createDirectory("Files")
            val currentFolder = tempFolder.createDirectory("S-BSST1_temp")
            currentFolder.createNewFile("test.txt")

            testMove(tempFolder.root.resolve("S-BSST1/Files/test.txt"), subFolder, currentFolder)
        }

        @Test
        fun `move from external folder`() {
            val subFolder = tempFolder.createDirectory("S-BSST1").createDirectory("Files")
            val currentFolder = tempFolder.createDirectory("S-BSST1_temp")
            val externalFolder = tempFolder.createDirectory("user-folder")
            val testFile = externalFolder.createNewFile("test.txt")

            testMove(testFile, subFolder, currentFolder)
        }

        @Test
        fun `move when already existing`() {
            val subFolder = tempFolder.createDirectory("S-BSST1").createDirectory("Files")
            val currentFolder = tempFolder.createDirectory("S-BSST1_temp")
            val testFile = subFolder.createNewFile("test.txt")

            testMove(testFile, subFolder, currentFolder)
        }

        private fun testMove(testFile: File, subFolder: File, currentFolder: File) {
            val extFile = NfsFile("test.txt", testFile)
            val config = FileProcessingConfig(subFolder, currentFolder, RW_R__R__, RWXR_XR_X)
            val result = config.nfsMove(extFile)

            assertThat(subFolder.resolve("test.txt")).exists()
            assertThat(result.file.path).isEqualTo("${tempFolder.root}/S-BSST1/Files/test.txt")
        }
    }
}
