package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
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
    @MockK val docFileRef: DocFileRef
) {
    private val testInstance = DocFileListConverter(docFileRefConverter)

    @Test
    fun convert() {
        every { docFileRefConverter.convert(documentFileRef) } returns docFileRef

        val result = testInstance.convert(createFileListDoc())
        assertThat(result).isInstanceOf(DocFileList::class.java)
        assertThat(result.fileName).isEqualTo(fileName)
        assertThat(result.files).isEqualTo(listOf(docFileRef))
    }

    private fun createFileListDoc(): Document {
        val fileList = Document()
        fileList[CommonsConverter.classField] = DocFileListFields.DOC_FILE_LIST_CLASS
        fileList[DocFileListFields.FILE_LIST_DOC_FILE_FILENAME] = fileName
        fileList[DocFileListFields.FILE_LIST_DOC_FILES] = listOf(documentFileRef)
        return fileList
    }

    companion object {
        const val fileName = "fileName"
    }
}
