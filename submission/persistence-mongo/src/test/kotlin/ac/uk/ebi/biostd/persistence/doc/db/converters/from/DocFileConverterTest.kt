package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.docFileClass
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DocFileConverterTest(
    @MockK val docAttributeConverter: DocAttributeConverter,
    @MockK val documentAttr: Document,
    @MockK val docAttribute: DocAttribute
) {
    private val testInstance = DocFileConverter(docAttributeConverter)

    @Test
    fun converter() {
        every { docAttributeConverter.convert(documentAttr) } returns docAttribute

        val result = testInstance.convert(createFileDoc())

        assertThat(result).isInstanceOf(DocFile::class.java)
        assertThat(result.filePath).isEqualTo(filePath)
        assertThat(result.attributes).isEqualTo(listOf(docAttribute))
        assertThat(result.md5).isEqualTo(md5)
    }

    private fun createFileDoc(): Document {
        val file = Document()
        file[CommonsConverter.classField] = docFileClass
        file[DocFileFields.FILE_DOC_FILE_PATH] = filePath
        file[DocFileFields.FILE_DOC_ATTRIBUTES] = listOf(documentAttr)
        file[DocFileFields.FILE_DOC_MD5] = md5
        return file
    }

    companion object {
        const val filePath = "filePath"
        const val attributes = "attributes"
        const val md5 = "md5"
    }
}
