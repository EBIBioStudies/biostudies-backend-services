package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)

internal class FileConverterTest(
    @MockK val attributeConverter: AttributeConverter,
    @MockK val document: Document,
    @MockK val docAttribute: DocAttribute
) {
    private val testInstance = FileConverter(attributeConverter)

    @Test
    fun converter() {
        every { attributeConverter.convert(docAttribute) } returns document

        val result = testInstance.convert(DocFile(DocFileFields.FILE_DOC_FILE_PATH, listOf(docAttribute), DocFileFields.FILE_DOC_MD5))

        assertThat(result[DocFileFields.FILE_DOC_FILE_PATH]).isEqualTo(docFileFilePath)
        assertThat(result[DocFileFields.FILE_DOC_ATTRIBUTES]).isEqualTo(listOf(document))
        assertThat(result[DocFileFields.FILE_DOC_MD5]).isEqualTo(docFileMd5)
    }

    private companion object {
        const val docFileFilePath = "filePath"
        const val docFileMd5 = "md5"
    }
}
