package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileTableFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DocFileTableConverterTest(
    @MockK val docFileConverter: DocFileConverter,
    @MockK val documentFile: Document,
    @MockK val docFile: DocFile
) {
    private val testInstance = DocFileTableConverter(docFileConverter)

    @Test
    fun convert() {
        every { docFileConverter.convert(documentFile) } returns docFile

        val result = testInstance.convert(createFileTableDoc())

        assertThat(result).isInstanceOf(DocFileTable::class.java)
        assertThat(result.files).isEqualTo(listOf(docFile))
    }

    private fun createFileTableDoc(): Document {
        val fileTable = Document()
        fileTable[CommonsConverter.classField] = DocFileTableFields.DOC_FILE_TABLE_CLASS
        fileTable[DocFileTableFields.FILE_TABLE_DOC_FILES] = listOf(documentFile)
        return fileTable
    }
}
