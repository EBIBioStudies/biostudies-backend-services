package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_REL_PATH
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
    @MockK val document: Document,
    @MockK val docAttribute: DocAttribute,
    @MockK val attributeConverter: AttributeConverter
) {
    private val testInstance = FileConverter(attributeConverter)

    @Test
    fun converter() {
        every { attributeConverter.convert(docAttribute) } returns document

        val result = testInstance.convert(
            DocFile(FILE_DOC_REL_PATH, FILE_DOC_FULL_PATH, listOf(docAttribute), FILE_DOC_MD5))

        assertThat(result[FILE_DOC_MD5]).isEqualTo("md5")
        assertThat(result[FILE_DOC_REL_PATH]).isEqualTo("relPath")
        assertThat(result[FILE_DOC_FULL_PATH]).isEqualTo("fullPath")
        assertThat(result[DocFileFields.FILE_DOC_ATTRIBUTES]).isEqualTo(listOf(document))
    }
}
