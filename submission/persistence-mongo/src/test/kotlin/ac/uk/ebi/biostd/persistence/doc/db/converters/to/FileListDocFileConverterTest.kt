package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
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
class FileListDocFileConverterTest(
    @MockK val documentFile: Document,
    @MockK val docFile: DocFile,
    @MockK val fileConverter: FileConverter
) {
    val testInstance = FileListDocFileConverter(fileConverter)

    @Test
    fun `fileListDocFile to document`() {
        val fileListDocFile = FileListDocFile(ObjectId(1, 1), ObjectId(1, 3), docFile)
        every { fileConverter.convert(docFile) } returns documentFile

        val result = testInstance.convert(fileListDocFile)

        assertThat(result[CommonsConverter.classField]).isEqualTo(fileListDocFileDocFileClass)
        assertThat(result[FileListDocFileFields.FILE_LIST_DOC_FILE_ID]).isEqualTo(ObjectId(1, 1))
        assertThat(result[FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID]).isEqualTo(ObjectId(1, 3))
        assertThat(result[FileListDocFileFields.FILE_LIST_DOC_FILE_FILE]).isEqualTo(documentFile)
    }
}
