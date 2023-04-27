package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.stream.FileListTsvStreamDeserializer
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import ebi.ac.uk.dsl.tsv.Tsv
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createTempFile
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(TemporaryFolderExtension::class)
class FileListTsvStreamDeserializerTest(
    private val tempFolder: TemporaryFolder,
) {
    private val testInstance = FileListTsvStreamDeserializer()

    @Test
    fun `deserialize file list with empty spaces`() {
        val tsvFile = createTsvFile(
            tsv {
                line("Files", "Type")
                line("testdir/test1.zarr", "zarr")
                line()
                line()
                line("testdir/test2.zarr", "zarr2")
                line()
                line()
            }
        )
        val files = tsvFile.inputStream().use { testInstance.deserializeFileList(it).toList() }

        assertThat(files).hasSize(2)

        assertThat(files.first()).isEqualTo(
            BioFile("testdir/test1.zarr", attributes = listOf(Attribute("Type", "zarr")))
        )
        assertThat(files.second()).isEqualTo(
            BioFile("testdir/test2.zarr", attributes = listOf(Attribute("Type", "zarr2")))
        )
    }

    @Test
    fun deserialize() {
        val tsvFile = createTsvFile(
            tsv {
                line("Files", "Attr1", "Attr2")
                line("file1.txt", "A", "B")
                line("file2.txt", "C", "D")
                line()
            }
        )

        val files = tsvFile.inputStream().use { testInstance.deserializeFileList(it).toList() }

        assertThat(files).hasSize(2)

        assertThat(files.first()).isEqualTo(
            BioFile("file1.txt", attributes = listOf(Attribute("Attr1", "A"), Attribute("Attr2", "B")))
        )
        assertThat(files.second()).isEqualTo(
            BioFile("file2.txt", attributes = listOf(Attribute("Attr1", "C"), Attribute("Attr2", "D")))
        )
    }

    private fun createTsvFile(content: Tsv): File {
        val file = tempFolder.root.createTempFile()
        file.writeText(content.toString())
        return file
    }

    @Test
    fun `serialize - deserialize FileList`() {
        var idx = 0
        fun bioFile(it: Int): BioFile {
            return BioFile(
                path = "folder/file$it.txt",
                attributes = listOf(Attribute("Attr1", "A$it"), Attribute("Attr2", "B$it"))
            )
        }

        val files = sequence { yield(bioFile(idx++)) }

        val output = tempFolder.createFile("testFile.tsv")
        output.outputStream().use { testInstance.serializeFileList(files, it) }

        output.inputStream().use {
            testInstance.deserializeFileList(it).forEachIndexed { idx, file ->
                assertThat(file).isEqualToComparingFieldByField(bioFile(idx))
            }
        }
    }

    @Test
    fun `file list with empty path`() {
        val tsv = tsv {
            line("Files", "Attr1", "Attr2")
            line("test.txt", "a", "b")
            line("", "c", "d")
            line()
        }

        val testFile = tempFolder.createFile("invalid.tsv", tsv.toString())
        testFile.inputStream().use {
            val exception = assertThrows<InvalidElementException> { testInstance.deserializeFileList(it).toList() }
            assertThat(exception.message).isEqualTo("Error at row 3: $REQUIRED_FILE_PATH. Element was not created.")
        }
    }
}
