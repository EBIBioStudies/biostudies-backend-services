package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields
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
    @MockK val fileDocument: Document,
    @MockK val docFile: DocFile,
) {
    private val testInstance = FileListConverter(fileConverter)

    @Test
    fun converter() {
        every { fileConverter.convert(docFile) } returns fileDocument

        val docFileList = DocFileList(DOC_FILE_LIST_FILE_NAME, listOf(docFile))

        val result = testInstance.convert(docFileList)

        assertThat(result[DocFileListFields.FILE_LIST_DOC_FILE_FILENAME]).isEqualTo(DOC_FILE_LIST_FILE_NAME)
        assertThat(result[DocFileListFields.FILE_LIST_DOC_PAGE_TAB_FILES]).isEqualTo(listOf(fileDocument))
    }

    private companion object {
        const val DOC_FILE_LIST_FILE_NAME = "fileName"
    }
}
