package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertFails

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class NfsFilesServiceTest(
    private val tempFolder: TemporaryFolder,
) {
    private val ftpFolder = tempFolder.createDirectory("ftp")
    private val subFolder = tempFolder.createDirectory("submission")
    private val folderResolver = SubmissionFolderResolver(subFolder.toPath(), ftpFolder.toPath())
    private val testInstance = NfsFilesService(folderResolver)

    @Test
    fun `persist submission file`() {
        val sub = basicExtSubmission
        val file = createNfsFile("file1.txt", "Files/file1.txt", tempFolder.createFile("file1.txt"))

        val persisted = testInstance.persistSubmissionFile(sub, file) as NfsFile

        assertThat(persisted.fullPath).isEqualTo("${subFolder.absolutePath}/${sub.relPath}/${file.relPath}")
        assertThat(Files.exists(Paths.get("${subFolder.absolutePath}/${sub.relPath}/${file.relPath}"))).isTrue()
    }

    @Test
    fun `delete submission files`() {
        val subFolder = subFolder.createDirectory("S-BSST1")
        val ftpFolder = ftpFolder.createDirectory("S-BSST1")
        val sub = basicExtSubmission.copy(relPath = "S-BSST1")

        testInstance.deleteSubmissionFiles(sub)

        assertThat(Files.exists(subFolder.toPath())).isFalse()
        assertThat(Files.exists(ftpFolder.toPath())).isFalse()
    }

    @Test
    fun `delete ftp links`() {
        val ftpFolder = ftpFolder.createDirectory("S-BSST2")
        val sub = basicExtSubmission.copy(relPath = "S-BSST2")

        testInstance.deleteFtpLinks(sub)

        assertThat(Files.exists(ftpFolder.toPath())).isFalse()
    }

    @Test
    fun `delete submission file`() {
        val subFolder = subFolder.createDirectory("S-BSST3")
        val filesFolder = subFolder.createDirectory("Files")
        val file = filesFolder.createFile("file1.txt")
        val nfsFile = createNfsFile("file1.txt", "Files/file1.txt", tempFolder.createFile("file1.txt"))

        val sub = basicExtSubmission.copy(relPath = "S-BSST3")
        testInstance.deleteSubmissionFile(sub, nfsFile)

        assertThat(Files.exists(file.toPath())).isFalse()
    }

    @Test
    fun `delete fire file`(
        @MockK fireFile: FireFile,
        @MockK submission: ExtSubmission,
    ) {
        val exception = assertFails { testInstance.deleteSubmissionFile(submission, fireFile) }
        assertThat(exception.message).isEqualTo("NfsFilesService should only handle NfsFile")
    }
}
