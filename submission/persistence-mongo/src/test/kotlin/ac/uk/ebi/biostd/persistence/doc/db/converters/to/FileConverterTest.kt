package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_DOC_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_FILE_SYSTEM
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_SIZE
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.FIRE
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.NFS
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
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
    fun `converter from nfsDocFile`() {
        every { attributeConverter.convert(docAttribute) } returns document
        val file =
            NfsDocFile(
                relPath = FILE_DOC_REL_PATH,
                fullPath = FILE_DOC_FULL_PATH,
                fileType = "file",
                attributes = listOf(docAttribute),
                md5 = FILE_DOC_MD5,
                fileSize = 10L,
                fileSystem = NFS
            )
        val result = testInstance.convert(file)

        assertThat(result[FILE_DOC_MD5]).isEqualTo("md5")
        assertThat(result[FILE_DOC_REL_PATH]).isEqualTo("relPath")
        assertThat(result[FILE_DOC_FULL_PATH]).isEqualTo("fullPath")
        assertThat(result[NfsDocFileFields.FILE_DOC_ATTRIBUTES]).isEqualTo(listOf(document))
        assertThat(result[NfsDocFileFields.FILE_TYPE]).isEqualTo("file")
        assertThat(result[ExtSerializationFields.FILE_SIZE]).isEqualTo(10L)
    }

    @Test
    fun `converter from FireDocFile`() {
        every { attributeConverter.convert(docAttribute) } returns document
        val file = FireDocFile(
            relPath = FIRE_FILE_DOC_REL_PATH,
            fireId = FIRE_FILE_DOC_ID,
            attributes = listOf(docAttribute),
            md5 = FILE_DOC_MD5,
            fileSize = 10L,
            fileSystem = FIRE
        )

        val result = testInstance.convert(file)

        assertThat(result[FIRE_FILE_DOC_REL_PATH]).isEqualTo("relPath")
        assertThat(result[FIRE_FILE_DOC_ID]).isEqualTo("fireId")
        assertThat(result[FIRE_FILE_DOC_ATTRIBUTES]).isEqualTo(listOf(document))
        assertThat(result[FIRE_FILE_DOC_MD5]).isEqualTo("md5")
        assertThat(result[FIRE_FILE_SIZE]).isEqualTo(10L)
        assertThat(result[FIRE_FILE_DOC_FILE_SYSTEM]).isEqualTo(FIRE.name)
    }
}
