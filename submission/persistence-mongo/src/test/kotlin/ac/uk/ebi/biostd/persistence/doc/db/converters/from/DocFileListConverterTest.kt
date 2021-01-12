package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.FileListConverter
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.docFileListClass
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
        assertThat(result).isInstanceOf(docFileListClazz)
        assertThat(result.fileName).isEqualTo(fileName)
        assertThat(result.files).isEqualTo(listOf(docFile))
    }

    private fun createFileListDoc(): Document {
        val fileList = Document()
        fileList[CommonsConverter.classField] = docFileListClass
        fileList[DocFileListConverter.docFileListFileName] = fileName
        fileList[DocFileListConverter.docFileListFiles] = listOf(documentFile)
        return fileList
    }

    companion object {
        val docFileListClazz = DocFileList::class.java
        const val fileName = "fileName"
    }
}
