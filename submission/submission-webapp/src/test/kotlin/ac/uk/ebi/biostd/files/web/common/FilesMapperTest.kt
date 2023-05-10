package ac.uk.ebi.biostd.files.web.common

import ac.uk.ebi.biostd.files.model.FilesSpec
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.UserFileType.DIR
import ebi.ac.uk.api.UserFileType.FILE
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Files

@ExtendWith(TemporaryFolderExtension::class)
class FilesMapperTest(
    private val tempFolder: TemporaryFolder
) {
    private val testInstance = FilesMapper()

    @Test
    fun `list user files`() {
        val dir = tempFolder.createDirectory("test-dir")
        val file = tempFolder.createFile("file1.txt", "some content")
        tempFolder.createFile("test-dir/file2.txt", "some other content")

        val fileSpec = FilesSpec(tempFolder.root.toPath(), listOf(dir, file))
        val userFiles = testInstance.asUserFiles(fileSpec)

        assertThat(userFiles).hasSize(2)

        val dirSize = Files.size(dir.toPath())
        val fileSize = Files.size(file.toPath())
        assertThat(userFiles.first()).isEqualToComparingFieldByField(UserFile("test-dir", "user", dirSize, DIR))
        assertThat(userFiles.second()).isEqualToComparingFieldByField(UserFile("file1.txt", "user", fileSize, FILE))
    }
}
