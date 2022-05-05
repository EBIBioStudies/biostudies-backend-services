package ebi.ac.uk.extended.model

import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@ExtendWith(TemporaryFolderExtension::class)
class ExtFileTest(private val temporaryFolder: TemporaryFolder) {
    @Test
    fun `extFile with file inside folder`() {
        val file = temporaryFolder.createDirectory("Files").createDirectory("my-folder").createNewFile("file.txt")
        val extFile = NfsFile(
            "/my-folder/file.txt", "Files/my-folder/file.txt",
            file,
            file.absolutePath,
            file.md5(),
            file.size(),
            listOf()
        )

        assertThat(extFile.fileName).isEqualTo("file.txt")
        assertThat(extFile.md5).isEqualTo(file.md5())
        assertThat(extFile.size).isEqualTo(file.size())
        assertThat(extFile.type).isEqualTo(FILE)
    }

    @Test
    fun `extFile with simple file`() {
        val file = temporaryFolder.createDirectory("Files").createNewFile("file.txt")
        val extFile = NfsFile(
            "file.txt", "Files/file.txt", file, file.absolutePath,
            file.md5(),
            file.size(),
            listOf()
        )

        assertThat(extFile.fileName).isEqualTo("file.txt")
        assertThat(extFile.md5).isEqualTo(file.md5())
        assertThat(extFile.size).isEqualTo(file.size())
    }

    @Test
    fun `extFile with directory`() {
        val file = temporaryFolder.createDirectory("folder")
        val extFile = NfsFile(
            "folder", "Files/folder", file, file.absolutePath,
            file.md5(),
            file.size(),
            listOf()
        )

        assertThat(extFile.fileName).isEqualTo("folder")
        assertThat(extFile.md5).isEqualTo(file.md5())
        assertThat(extFile.size).isEqualTo(file.size())
        assertThat(extFile.type).isEqualTo(DIR)
    }

    @ParameterizedTest
    @EnumSource(ExtFileType::class)
    fun `ext file type from string`(extFileType: ExtFileType) {
        assertThat(ExtFileType.fromString(extFileType.value)).isEqualTo(extFileType)
    }

    @Test
    fun `invalid ext file type`() {
        val exception = assertThrows<IllegalArgumentException> { ExtFileType.fromString("invalid") }
        assertThat(exception.message).isEqualTo("Unknown ExtFileType 'invalid'")
    }
}
