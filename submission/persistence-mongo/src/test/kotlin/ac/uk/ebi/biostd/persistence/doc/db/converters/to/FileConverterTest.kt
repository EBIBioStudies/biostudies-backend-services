package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FILENAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FILEPATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5_CALCULATED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
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
    @MockK val attributeConverter: AttributeConverter,
) {
    private val testInstance = FileConverter(attributeConverter)

    @Test
    fun `converter from nfs doc file`() {
        every { attributeConverter.convert(docAttribute) } returns document
        val file =
            NfsDocFile(
                fileName = FILE_DOC_FILENAME,
                filePath = FILE_DOC_FILEPATH,
                relPath = FILE_DOC_REL_PATH,
                fullPath = NFS_FILE_FULL_PATH,
                attributes = listOf(docAttribute),
                md5Calculated = true,
                md5 = FILE_DOC_MD5,
                fileSize = 10L,
                fileType = "file",
            )
        val result = testInstance.convert(file)

        assertThat(result[FILE_DOC_FILENAME]).isEqualTo("fileName")
        assertThat(result[FILE_DOC_FILEPATH]).isEqualTo("filePath")
        assertThat(result[FILE_DOC_REL_PATH]).isEqualTo("relPath")
        assertThat(result[NFS_FILE_FULL_PATH]).isEqualTo("fullPath")
        assertThat(result[FILE_DOC_ATTRIBUTES]).isEqualTo(listOf(document))
        assertThat(result[FILE_DOC_MD5]).isEqualTo("md5")
        assertThat(result[FILE_DOC_SIZE]).isEqualTo(10L)
        assertThat(result[FILE_DOC_TYPE]).isEqualTo("file")
        assertThat(result[FILE_DOC_MD5_CALCULATED]).isEqualTo(true)
    }

    @Test
    fun `converter from fire doc file`() {
        every { attributeConverter.convert(docAttribute) } returns document
        val file =
            FireDocFile(
                fileName = FILE_DOC_FILENAME,
                filePath = FILE_DOC_FILEPATH,
                relPath = FILE_DOC_REL_PATH,
                fireId = FIRE_FILE_DOC_ID,
                attributes = listOf(docAttribute),
                md5 = FILE_DOC_MD5,
                fileSize = 10L,
                fileType = FILE.value,
            )

        val result = testInstance.convert(file)

        assertThat(result[FILE_DOC_FILENAME]).isEqualTo("fileName")
        assertThat(result[FILE_DOC_FILEPATH]).isEqualTo("filePath")
        assertThat(result[FILE_DOC_REL_PATH]).isEqualTo("relPath")
        assertThat(result[FIRE_FILE_DOC_ID]).isEqualTo("fireId")
        assertThat(result[FILE_DOC_ATTRIBUTES]).isEqualTo(listOf(document))
        assertThat(result[FILE_DOC_MD5]).isEqualTo("md5")
        assertThat(result[FILE_DOC_SIZE]).isEqualTo(10L)
        assertThat(result[FILE_DOC_TYPE]).isEqualTo(FILE.value)
    }

    @Test
    fun `converter from fire doc directory`() {
        every { attributeConverter.convert(docAttribute) } returns document
        val file =
            FireDocFile(
                fileName = "fire-directory",
                filePath = FILE_DOC_FILEPATH,
                relPath = FILE_DOC_REL_PATH,
                fireId = "dirFireId",
                attributes = listOf(docAttribute),
                md5 = FILE_DOC_MD5,
                fileSize = 10L,
                fileType = DIR.value,
            )

        val result = testInstance.convert(file)

        assertThat(result[FILE_DOC_FILENAME]).isEqualTo("fire-directory")
        assertThat(result[FILE_DOC_FILEPATH]).isEqualTo("filePath")
        assertThat(result[FILE_DOC_REL_PATH]).isEqualTo("relPath")
        assertThat(result[FIRE_FILE_DOC_ID]).isEqualTo("dirFireId")
        assertThat(result[FILE_DOC_ATTRIBUTES]).isEqualTo(listOf(document))
        assertThat(result[FILE_DOC_MD5]).isEqualTo("md5")
        assertThat(result[FILE_DOC_SIZE]).isEqualTo(10L)
        assertThat(result[FILE_DOC_TYPE]).isEqualTo(DIR.value)
    }
}
