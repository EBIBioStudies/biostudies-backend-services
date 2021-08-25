package ac.uk.ebi.biostd.persistence.nfs

import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFileProcessingConfig
import ac.uk.ebi.biostd.persistence.filesystem.nfs.nfsCopy
import ac.uk.ebi.biostd.persistence.filesystem.nfs.nfsMove
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.test.clean
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class NfsFileProcessingConfigTest(private val tempFolder: TemporaryFolder) {
    @AfterEach
    fun afterEach() = tempFolder.clean()

    @Test
    fun copy() {
        val subFolder = tempFolder.createDirectory("subFolder")
        val targetFolder = tempFolder.createDirectory("target")
        val extFile = NfsFile("test.txt", tempFolder.createFile("test.txt"))
        val config = NfsFileProcessingConfig(COPY, subFolder, targetFolder, RW_R__R__, RWXR_XR_X)

        val result = config.nfsCopy(extFile)

        assertThat(targetFolder.resolve("test.txt")).exists()
        assertThat(result.file.path).isEqualTo("${subFolder.absolutePath}/test.txt")
        assertThat(extFile.file).exists()
    }

    @Test
    fun move() {
        val subFolder = tempFolder.createDirectory("subFolder")
        val targetFolder = tempFolder.createDirectory("target")
        val extFile = NfsFile("test.txt", tempFolder.createFile("test.txt"))
        val config = NfsFileProcessingConfig(COPY, subFolder, targetFolder, RW_R__R__, RWXR_XR_X)

        val result = config.nfsMove(extFile)

        assertThat(targetFolder.resolve("test.txt")).exists()
        assertThat(result.file.path).isEqualTo("${subFolder.absolutePath}/test.txt")
        assertThat(extFile.file).doesNotExist()
    }
}
