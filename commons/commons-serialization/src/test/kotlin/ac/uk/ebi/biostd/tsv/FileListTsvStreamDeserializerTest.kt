package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.stream.FileListTsvStreamDeserializer
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

const val TSV_FILE_LIST = "FileList.tsv"

@ExtendWith(TemporaryFolderExtension::class)
class FileListTsvStreamDeserializerTest(
    private val tempFolder: TemporaryFolder
) {
    private val testInstance = FileListTsvStreamDeserializer()
    private val tsvFile = tempFolder.createFile(TSV_FILE_LIST)
    private val fileSystem = tempFolder.createFile("testFile.tsv")

    @BeforeEach
    fun beforeEach() {
        val tsv = tsv {
            line("Files", "Attr1", "Attr2")
            line("file1.txt", "A", "B")
            line("file2.txt", "C", "D")
            line()
        }

        tsvFile.writeText(tsv.toString())
    }

    @Test
    fun deserialize() {
        val files = tsvFile.inputStream().use { testInstance.deserializeFileList(it).toList() }

        assertThat(files).hasSize(2)

        assertThat(files.first()).isEqualTo(
            BioFile("file1.txt", attributes = listOf(Attribute("Attr1", "A"), Attribute("Attr2", "B")))
        )
        assertThat(files.second()).isEqualTo(
            BioFile("file2.txt", attributes = listOf(Attribute("Attr1", "C"), Attribute("Attr2", "D")))
        )
    }

    @Test
    fun `serialize - deserialize FileList`() {
        fun bioFile(it: Int) =
            BioFile("folder/file$it.txt", attributes = listOf(Attribute("Attr1", "A$it"), Attribute("Attr2", "B$it")))

        var idx = -1
        val files = object : Sequence<BioFile> {
            override fun iterator(): Iterator<BioFile> = object : Iterator<BioFile> {
                override fun hasNext(): Boolean = idx < 20_000

                override fun next(): BioFile {
                    idx++
                    return bioFile(idx)
                }
            }
        }

        fileSystem.outputStream().use { testInstance.serializeFileList(files, it) }

        fileSystem.inputStream().use {
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
