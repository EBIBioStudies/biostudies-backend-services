package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_DOC_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_TYPE
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.FireDocDirectory
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
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
    fun `converter from nfs doc file`() {
        every { attributeConverter.convert(docAttribute) } returns document
        val file =
            NfsDocFile(
                relPath = NFS_FILE_DOC_REL_PATH,
                fullPath = NFS_FILE_DOC_FULL_PATH,
                fileType = "file",
                attributes = listOf(docAttribute),
                md5 = FILE_DOC_MD5,
                fileSize = 10L
            )
        val result = testInstance.convert(file)

        assertThat(result[NFS_FILE_DOC_REL_PATH]).isEqualTo("relPath")
        assertThat(result[NFS_FILE_DOC_FULL_PATH]).isEqualTo("fullPath")
        assertThat(result[NFS_FILE_TYPE]).isEqualTo("file")
        assertThat(result[FILE_DOC_ATTRIBUTES]).isEqualTo(listOf(document))
        assertThat(result[FILE_DOC_MD5]).isEqualTo("md5")
        assertThat(result[FILE_DOC_SIZE]).isEqualTo(10L)
    }

    @Test
    fun `converter from fire doc file`() {
        every { attributeConverter.convert(docAttribute) } returns document
        val file = FireDocFile(
            fileName = FIRE_FILE_DOC_FILE_NAME,
            fireId = FIRE_FILE_DOC_ID,
            attributes = listOf(docAttribute),
            md5 = FILE_DOC_MD5,
            fileSize = 10L
        )

        val result = testInstance.convert(file)

        assertThat(result[FIRE_FILE_DOC_FILE_NAME]).isEqualTo("fileName")
        assertThat(result[FIRE_FILE_DOC_ID]).isEqualTo("fireId")
        assertThat(result[FILE_DOC_ATTRIBUTES]).isEqualTo(listOf(document))
        assertThat(result[FILE_DOC_MD5]).isEqualTo("md5")
        assertThat(result[FILE_DOC_SIZE]).isEqualTo(10L)
    }

    @Test
    fun `converter from fire doc directory`() {
        every { attributeConverter.convert(docAttribute) } returns document
        val file = FireDocDirectory(
            fileName = "fire-directory",
            attributes = listOf(docAttribute),
            md5 = "md5",
            fileSize = 10L
        )

        val result = testInstance.convert(file)

        assertThat(result[FIRE_FILE_DOC_FILE_NAME]).isEqualTo("fire-directory")
        assertThat(result[FILE_DOC_ATTRIBUTES]).isEqualTo(listOf(document))
        assertThat(result[FILE_DOC_MD5]).isEqualTo("md5")
        assertThat(result[FILE_DOC_SIZE]).isEqualTo(10L)
    }
}
