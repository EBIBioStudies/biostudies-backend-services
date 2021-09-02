package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_LIST_DOC_FILE_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.model.fireDocFileClass
import ac.uk.ebi.biostd.persistence.doc.model.nfsDocFileClass
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DocFileConverterTest(
    @MockK val documentAttr: Document,
    @MockK val docAttribute: DocAttribute,
    @MockK val docAttributeConverter: DocAttributeConverter
) {
    private val testInstance = DocFileConverter(docAttributeConverter)

    @Test
    fun `convert to NfsDocFile`() {
        every { docAttributeConverter.convert(documentAttr) } returns docAttribute

        val result = testInstance.convert(createNfsFileDoc())

        require(result is NfsDocFile)
        assertThat(result.relPath).isEqualTo("relPath")
        assertThat(result.fullPath).isEqualTo("location")
        assertThat(result.attributes).isEqualTo(listOf(docAttribute))
        assertThat(result.md5).isEqualTo("md5")
        assertThat(result.fileType).isEqualTo("file")
        assertThat(result.fileSize).isEqualTo(10L)
    }

    @Test
    fun `convert to FireDocFile`() {
        every { docAttributeConverter.convert(documentAttr) } returns docAttribute

        val result = testInstance.convert(createFireFileDoc())

        require(result is FireDocFile)
        assertThat(result.fileName).isEqualTo("fileName")
        assertThat(result.fireId).isEqualTo("fireId")
        assertThat(result.attributes).isEqualTo(listOf(docAttribute))
        assertThat(result.md5).isEqualTo("md5")
        assertThat(result.fileSize).isEqualTo(10L)
    }

    private fun createNfsFileDoc(): Document {
        val file = Document()

        file[CommonsConverter.classField] = nfsDocFileClass
        file[NFS_FILE_DOC_REL_PATH] = "relPath"
        file[FILE_LIST_DOC_FILE_FULL_PATH] = "location"
        file[NFS_FILE_TYPE] = "file"
        file[FILE_DOC_ATTRIBUTES] = listOf(documentAttr)
        file[FILE_DOC_MD5] = "md5"
        file[FILE_DOC_SIZE] = 10L

        return file
    }

    private fun createFireFileDoc(): Document {
        val file = Document()

        file[CommonsConverter.classField] = fireDocFileClass
        file[FIRE_FILE_DOC_FILE_NAME] = "fileName"
        file[FIRE_FILE_DOC_ID] = "fireId"
        file[FILE_DOC_ATTRIBUTES] = listOf(documentAttr)
        file[FILE_DOC_MD5] = "md5"
        file[FILE_DOC_SIZE] = 10L

        return file
    }
}
