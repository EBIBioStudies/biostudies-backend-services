package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class)
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
        assertThat(Files.exists(Paths.get("${subFolder.absolutePath}/${sub.relPath}_temp/${file.relPath}"))).isTrue()
    }

    @Test
    fun `post process submission files`() {
        val sub = basicExtSubmission
        val file = createNfsFile("file2.txt", "Files/file2.txt", tempFolder.createFile("file2.txt"))

        testInstance.persistSubmissionFile(sub, file) as NfsFile
        testInstance.postProcessSubmissionFiles(sub)

        assertThat(Files.exists(Paths.get("${subFolder.absolutePath}/${sub.relPath}/${file.relPath}"))).isTrue()
        assertThat(Files.exists(Paths.get("${subFolder.absolutePath}/${sub.relPath}_temp/${file.relPath}"))).isFalse()
    }
}
