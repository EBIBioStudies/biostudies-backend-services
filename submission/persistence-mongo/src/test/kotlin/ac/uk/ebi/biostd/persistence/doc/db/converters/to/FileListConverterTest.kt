package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class FileListConverterTest(
    @MockK val fileConverter: FileConverter,
    @MockK val document: Document,
    @MockK val docFile: DocFile
) {

    private val testInstance = FileListConverter(fileConverter)
    @Test
    fun converter() {
        val files = listOf(docFile)
        every { fileConverter.convert(docFile) } returns document

        val docFileList = DocFileList(docFileListFileName, files)

        val result = testInstance.convert(docFileList)

        assertThat(result[FileListConverter.fileListDocFileName]).isEqualTo(docFileListFileName)
        assertThat(result[FileListConverter.fileListDocFiles]).isEqualTo(listOf(document))
    }

    private companion object {
        const val docFileListFileName = "fileName"
    }
}
