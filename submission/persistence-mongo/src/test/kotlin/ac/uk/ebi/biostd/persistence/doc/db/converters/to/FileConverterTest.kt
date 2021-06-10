package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.NFS
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields

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
        val file = DocFile(FILE_DOC_REL_PATH, FILE_DOC_FULL_PATH, listOf(docAttribute), FILE_DOC_MD5, "file", 10L, NFS)
        val result = testInstance.convert(file)

        assertThat(result[FILE_DOC_MD5]).isEqualTo("md5")
        assertThat(result[FILE_DOC_REL_PATH]).isEqualTo("relPath")
        assertThat(result[FILE_DOC_FULL_PATH]).isEqualTo("fullPath")
        assertThat(result[DocFileFields.FILE_DOC_ATTRIBUTES]).isEqualTo(listOf(document))
        assertThat(result[DocFileFields.FILE_TYPE]).isEqualTo("file")
        assertThat(result[ExtSerializationFields.FILE_SIZE]).isEqualTo(10L)
    }
}
