package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileRef
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class FileListConverterTest(
    @MockK val fileRefConverter: FileRefConverter,
    @MockK val document: Document,
    @MockK val docRefFile: DocFileRef,
    @MockK val submissionID: ObjectId
) {
    private val testInstance = FileListConverter(fileRefConverter)
    @Test
    fun converter() {
        val files = listOf(docRefFile)
        every { fileRefConverter.convert(docRefFile) } returns document

        val docFileList = DocFileList(docFileListFileName, files)

        val result = testInstance.convert(docFileList)

        assertThat(result[DocFileListFields.FILE_LIST_DOC_FILE_FILENAME]).isEqualTo(docFileListFileName)
        assertThat(result[DocFileListFields.FILE_LIST_DOC_FILES]).isEqualTo(listOf(document))
    }

    @Test
    fun `simple converter`() {
        val files = listOf(docRefFile)
        every { fileRefConverter.convert(docRefFile) } returns document

        val docFileList = DocFileList(docFileListFileName, files)

        val result = testInstance.convert(docFileList)

        assertThat(result[DocFileListFields.FILE_LIST_DOC_FILE_FILENAME]).isEqualTo(docFileListFileName)
        assertThat(result[DocFileListFields.FILE_LIST_DOC_FILES]).isEqualTo(listOf(document))
    }

    private companion object {
        const val docFileListFileName = "fileName"
    }
}
