package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.stream.FileListTsvStreamDeserializer
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

const val TSV_FILE_LIST = "FileList.tsv"

@ExtendWith(TemporaryFolderExtension::class)
class FileListTsvStreamDeserializerTest(temporaryFolder: TemporaryFolder) {
    private val testInstance = FileListTsvStreamDeserializer()
    private val tsvFile = temporaryFolder.createFile(TSV_FILE_LIST)
    private val fileSystem = temporaryFolder.createFile("testFile.txt")

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
        val files = testInstance.deserializeFileList(tsvFile).toList()

        assertThat(files).hasSize(2)

        assertThat(files.first()).isEqualTo(
            File("file1.txt", attributes = listOf(Attribute("Attr1", "A"), Attribute("Attr2", "B")))
        )
        assertThat(files.second()).isEqualTo(
            File("file2.txt", attributes = listOf(Attribute("Attr1", "C"), Attribute("Attr2", "D")))
        )
    }

    @Test
    fun `serialize - deserialize FileList`() {
        val files = (1..50000).map {
            File("folder/file$it.txt", attributes = listOf(Attribute("Attr1", "A$it"), Attribute("Attr2", "B$it")))
        }.asSequence()
        val iterator = files.iterator()

        testInstance.serializeFileList(files, fileSystem)

        testInstance.deserializeFileList(fileSystem).forEach { file ->
            assertThat(file).isEqualToComparingFieldByField(iterator.next())
        }
    }
}
