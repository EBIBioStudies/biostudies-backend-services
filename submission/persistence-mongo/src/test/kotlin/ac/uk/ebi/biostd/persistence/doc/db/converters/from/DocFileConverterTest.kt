package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_DOC_DIRECTORY_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.FILE_LIST_DOC_FILE_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_DOC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_FILE_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.FireDocDirectory
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

    @Test
    fun `convert to FireDirectoryFile`() {
        every { docAttributeConverter.convert(documentAttr) } returns docAttribute

        val result = testInstance.convert(createFireDirectoryDoc())

        require(result is FireDocDirectory)
        assertThat(result.fileName).isEqualTo("fire-directory")
        assertThat(result.attributes).isEqualTo(listOf(docAttribute))
        assertThat(result.md5).isEqualTo("md5")
        assertThat(result.fileSize).isEqualTo(10L)
    }

    private fun createNfsFileDoc() = Document().apply {
        this[CommonsConverter.classField] = nfsDocFileClass
        this[NFS_FILE_DOC_REL_PATH] = "relPath"
        this[FILE_LIST_DOC_FILE_FULL_PATH] = "location"
        this[NFS_FILE_TYPE] = "file"
        this[FILE_DOC_ATTRIBUTES] = listOf(documentAttr)
        this[FILE_DOC_MD5] = "md5"
        this[FILE_DOC_SIZE] = 10L
    }

    private fun createFireFileDoc() = Document().apply {
        this[CommonsConverter.classField] = fireDocFileClass
        this[FIRE_FILE_DOC_FILE_NAME] = "fileName"
        this[FIRE_FILE_DOC_ID] = "fireId"
        this[FILE_DOC_ATTRIBUTES] = listOf(documentAttr)
        this[FILE_DOC_MD5] = "md5"
        this[FILE_DOC_SIZE] = 10L
    }

    private fun createFireDirectoryDoc() = Document().apply {
        this[CommonsConverter.classField] = FIRE_DOC_DIRECTORY_CLASS
        this[FIRE_FILE_DOC_FILE_NAME] = "fire-directory"
        this[FILE_DOC_ATTRIBUTES] = listOf(documentAttr)
        this[FILE_DOC_MD5] = "md5"
        this[FILE_DOC_SIZE] = 10L
    }
}
