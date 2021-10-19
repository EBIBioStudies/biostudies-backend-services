package ac.uk.ebi.biostd.persistence.nfs

import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFileProcessingConfig
import ac.uk.ebi.biostd.persistence.filesystem.nfs.nfsCopy
import ac.uk.ebi.biostd.persistence.filesystem.nfs.nfsMove
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RW_R__R__
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
    private lateinit var file: File
    private lateinit var subFolder: File
    private lateinit var targetFolder: File
    private lateinit var extFile: NfsFile

    @AfterEach
    fun afterEach() = tempFolder.clean()

    @BeforeEach
    fun beforeEach() {
        file = tempFolder.createFile("test.txt")
        subFolder = tempFolder.createDirectory("subFolder")
        targetFolder = tempFolder.createDirectory("target")
        extFile = NfsFile("test.txt", file)
    }

    @Test
    fun copy() {
        val permissions = Permissions(RW_R__R__, RWXR_XR_X)
        val config = NfsFileProcessingConfig("S-BSST1", "user@mail.org", COPY, subFolder, targetFolder, permissions)

        val result = config.nfsCopy(extFile)

        assertThat(targetFolder.resolve("test.txt")).exists()
        assertThat(result.file.path).isEqualTo("${subFolder.absolutePath}/test.txt")
        assertThat(extFile.file).exists()
    }

    @Test
    fun `copy when exists`() {
        file.copyTo(subFolder.resolve(file.name))
        val permissions = Permissions(RW_R__R__, RWXR_XR_X)
        val config = NfsFileProcessingConfig("S-BSST1", "user@mail.org", COPY, subFolder, targetFolder, permissions)

        val result = config.nfsCopy(extFile)

        assertThat(targetFolder.resolve("test.txt")).exists()
        assertThat(result.file.path).isEqualTo("${subFolder.absolutePath}/test.txt")
        assertThat(extFile.file).exists()
        assertThat(subFolder.resolve(file.name)).doesNotExist()
    }

    @Test
    fun move() {
        val permissions = Permissions(RW_R__R__, RWXR_XR_X)
        val config = NfsFileProcessingConfig("S-BSST1", "user@mail.org", COPY, subFolder, targetFolder, permissions)

        val result = config.nfsMove(extFile)

        assertThat(targetFolder.resolve("test.txt")).exists()
        assertThat(result.file.path).isEqualTo("${subFolder.absolutePath}/test.txt")
        assertThat(extFile.file).doesNotExist()
    }
}
