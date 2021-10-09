package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileRef
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.bson.Document
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DocFileListConverterTest(
    @MockK val docFileRefConverter: DocFileRefConverter,
    @MockK val documentFileRef: Document,
    @MockK val docFileRef: DocFileRef,

    @MockK val docFileConverter: DocFileConverter,
    @MockK val documentTabFile: Document,
    @MockK val docFile: DocFile
) {
    private val testInstance = DocFileListConverter(docFileRefConverter, docFileConverter)

    @Test
    fun convert() {
        every { docFileRefConverter.convert(documentFileRef) } returns docFileRef
        every { docFileConverter.convert(documentTabFile) } returns docFile

        val result = testInstance.convert(createFileListDoc(documentFileRef, documentTabFile))

        assertThat(result).isInstanceOf(DocFileList::class.java)
        assertThat(result.fileName).isEqualTo(fileName)
        assertThat(result.files).isEqualTo(listOf(docFileRef))
        assertThat(result.pageTabFiles).isEqualTo(listOf(docFile))
    }

    private fun createFileListDoc(documentFileRef: Document, documentFile: Document): Document {
        val fileList = Document()
        fileList[CommonsConverter.classField] = DocFileListFields.DOC_FILE_LIST_CLASS
        fileList[DocFileListFields.FILE_LIST_DOC_FILE_FILENAME] = fileName
        fileList[DocFileListFields.FILE_LIST_DOC_FILES] = listOf(documentFileRef)
        fileList[DocFileListFields.FILE_LIST_DOC_PAGE_TAB_FILES] = listOf(documentFile)
        return fileList
    }

    companion object {
        const val fileName = "fileName"
    }
}
