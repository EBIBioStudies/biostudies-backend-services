package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.stream.FileListTsvStreamDeserializer
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv
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
        val fileList = testInstance.deserialize(tsvFile)

        assertThat(fileList.name).isEqualTo(TSV_FILE_LIST)
        assertThat(fileList.referencedFiles).hasSize(2)

        assertThat(fileList.referencedFiles.first()).isEqualTo(
            File("file1.txt", attributes = listOf(Attribute("Attr1", "A"), Attribute("Attr2", "B"))))
        assertThat(fileList.referencedFiles.second()).isEqualTo(
            File("file2.txt", attributes = listOf(Attribute("Attr1", "C"), Attribute("Attr2", "D"))))
    }
}
