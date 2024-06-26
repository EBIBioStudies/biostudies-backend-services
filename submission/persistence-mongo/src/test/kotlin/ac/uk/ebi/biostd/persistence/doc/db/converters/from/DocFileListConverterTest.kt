package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
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
internal class DocFileListConverterTest(
    @MockK val docFileConverter: DocFileConverter,
    @MockK val documentTabFile: Document,
    @MockK val docFile: DocFile,
) {
    private val testInstance = DocFileListConverter(docFileConverter)

    @Test
    fun convert() {
        every { docFileConverter.convert(documentTabFile) } returns docFile

        val result = testInstance.convert(createFileListDoc(documentTabFile))

        assertThat(result).isInstanceOf(DocFileList::class.java)
        assertThat(result.fileName).isEqualTo(FILE_NAME)
        assertThat(result.pageTabFiles).isEqualTo(listOf(docFile))
    }

    private fun createFileListDoc(documentFile: Document): Document {
        val fileList = Document()
        fileList[CommonsConverter.CLASS_FIELD] = DocFileListFields.DOC_FILE_LIST_CLASS
        fileList[DocFileListFields.FILE_LIST_DOC_FILE_FILENAME] = FILE_NAME
        fileList[DocFileListFields.FILE_LIST_DOC_PAGE_TAB_FILES] = listOf(documentFile)
        return fileList
    }

    companion object {
        const val FILE_NAME = "fileName"
    }
}
