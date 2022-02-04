package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.fileListDocFileDocFileClass
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DocFileListDocFileConverterTest(
    @MockK val documentFile: Document,
    @MockK val docFile: DocFile,
    @MockK val docFileConverter: DocFileConverter
) {
    val testInstance = DocFileListDocFileConverter(docFileConverter)

    @Test
    fun `convert to FileListDocFile`() {
        every { docFileConverter.convert(documentFile) } returns docFile

        val result = testInstance.convert(createFileListDocFile())

        assertThat(result.id).isEqualTo(ObjectId(1, 1))
        assertThat(result.submissionId).isEqualTo(ObjectId(1, 2))
        assertThat(result.file).isEqualTo(docFile)
        assertThat(result.fileListName).isEqualTo("fileList.txt")
        assertThat(result.index).isEqualTo(1)
        assertThat(result.submissionAccNo).isEqualTo("TEST_123")
        assertThat(result.submissionVersion).isEqualTo(2)
    }

    private fun createFileListDocFile() = Document().apply {

        this[CommonsConverter.classField] = fileListDocFileDocFileClass
        this[FileListDocFileFields.FILE_LIST_DOC_FILE_ID] = ObjectId(1, 1)
        this[FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID] = ObjectId(1, 2)
        this[FileListDocFileFields.FILE_LIST_DOC_FILE_FILE] = documentFile
        this[FileListDocFileFields.FILE_LIST_DOC_FILE_FILE_LIST] = "fileList.txt"
        this[FileListDocFileFields.FILE_LIST_DOC_FILE_INDEX] = 1
        this[FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO] = "TEST_123"
        this[FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_VERSION] = 2
    }
}
