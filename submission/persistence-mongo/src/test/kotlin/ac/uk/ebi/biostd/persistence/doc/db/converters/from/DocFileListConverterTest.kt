package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.bson.Document
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DocFileListConverterTest(
    @MockK val docFileConverter: DocFileConverter,
    @MockK val documentFile: Document,
    @MockK val docFile: DocFile
) {
    private val testInstance = DocFileListConverter(docFileConverter)

    @Test
    fun convert() {
        every { docFileConverter.convert(documentFile) } returns docFile

        val result = testInstance.convert(createFileListDoc())
        assertThat(result).isInstanceOf(DocFileList::class.java)
        assertThat(result.fileName).isEqualTo(fileName)
        assertThat(result.files).isEqualTo(listOf(docFile))
    }

    private fun createFileListDoc(): Document {
        val fileList = Document()
        fileList[CommonsConverter.classField] = DocFileListFields.DOC_FILE_LIST_CLASS
        fileList[DocFileListFields.FILE_LIST_DOC_FILE_LIST] = fileName
        fileList[DocFileListFields.FILE_LIST_DOC_FILES] = listOf(documentFile)
        return fileList
    }

    companion object {
        const val fileName = "fileName"
    }
}
