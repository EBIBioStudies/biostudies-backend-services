package ac.uk.ebi.biostd.persistence.doc.db.converters.from

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

        assertThat(result).isInstanceOf(docFileClazz)
        assertThat(result.filePath).isEqualTo(filePath)
        assertThat(result.attributes).isEqualTo(listOf(docAttribute))
        assertThat(result.md5).isEqualTo(md5)
    }

    private fun createFileDoc(): Document {
        val file = Document()
        file[CommonsConverter.classField] = docFileClass
        file[DocFileConverter.docFileFilePath] = filePath
        file[DocFileConverter.docFileAttributes] = listOf(documentAttr)
        file[DocFileConverter.docFileMd5] = md5
        return file
    }

    companion object {
        val docFileClazz = DocFile::class.java
        const val filePath = "filePath"
        const val attributes = "attributes"
        const val md5 = "md5"
    }
}
