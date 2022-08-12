package ac.uk.ebi.biostd.persistence.nfs

import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFileProcessingConfig
import ac.uk.ebi.biostd.persistence.filesystem.nfs.nfsCopy
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.io.ext.newFile
import ebi.ac.uk.test.clean
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(TemporaryFolderExtension::class)
class NfsFileProcessingConfigTest(private val tempFolder: TemporaryFolder) {
    private lateinit var folder: File
    private lateinit var file: File
    private lateinit var subFolder: File
    private lateinit var targetFolder: File
    private lateinit var extFile: NfsFile
    private val permissions = Permissions(RW_R__R__, RWXR_XR_X)

    @AfterEach
    fun afterEach() = tempFolder.clean()

    @BeforeEach
    fun beforeEach() {
        folder = tempFolder.createDirectory("folder")
        file = folder.newFile("test.txt")
        subFolder = tempFolder.createDirectory("subFolder")
        targetFolder = tempFolder.createDirectory("target")
        extFile = createNfsFile("folder/test.txt", "Files/folder/test.txt", file)
    }

    @Test
    fun copy() {
        val fileSubFolder = subFolder.resolve("folder/test.txt")
        assertThat(fileSubFolder).doesNotExist()

        val config = NfsFileProcessingConfig("S-BSST1", "user@mail.org", subFolder, targetFolder, permissions)
        val result = config.nfsCopy(extFile)

        assertThat(fileSubFolder).doesNotExist()
        assertThat(targetFolder.resolve("folder/test.txt")).exists()
        assertThat(result.file).isEqualTo(fileSubFolder)
        assertThat(result.fullPath).isEqualTo(fileSubFolder.absolutePath)
        assertThat(extFile.file).exists()
    }

    @Test
    fun `copy when exists`() {
        folder.copyRecursively(subFolder.resolve(folder.name))
        val fileSubFolder = subFolder.resolve("folder/test.txt")
        assertThat(fileSubFolder).exists()

        val config = NfsFileProcessingConfig("S-BSST1", "user@mail.org", subFolder, targetFolder, permissions)
        val result = config.nfsCopy(extFile)

        assertThat(fileSubFolder).doesNotExist()
        assertThat(targetFolder.resolve("folder/test.txt")).exists()
        assertThat(result.file).isEqualTo(fileSubFolder)
        assertThat(result.fullPath).isEqualTo(fileSubFolder.absolutePath)
        assertThat(extFile.file).exists()
    }
}
