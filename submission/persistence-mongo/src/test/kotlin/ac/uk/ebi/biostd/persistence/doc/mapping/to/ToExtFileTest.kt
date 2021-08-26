package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.NFS
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.basicDocAttribute
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.assertExtFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.assertExtFileList
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileList
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.fireDocFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.nfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.TEST_REL_PATH
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@Disabled
@ExtendWith(TemporaryFolderExtension::class)
class ToExtFileTest(temporaryFolder: TemporaryFolder) {
    private val testFile = temporaryFolder.createFile(TEST_REL_PATH)
    private val testNfsDocFile = nfsDocFile.copy(fullPath = testFile.absolutePath)
    private val testFireDocFile = fireDocFile

    @Test
    fun `nfsDocFile to ext file`() {
        val extFile = testNfsDocFile.toExtFile()
        assertExtFile(extFile, testFile)
    }

    @Test
    fun `fireDocFile to ext file`() {
        val extFile = testFireDocFile.toExtFile()
        assertExtFile(extFile, testFile)
    }

    @Test
    fun `to ext file table`() {
        val docFilesTable = DocFileTable(listOf(testNfsDocFile, testFireDocFile))
        val extFilesTable = docFilesTable.toExtFileTable()

        assertThat(extFilesTable.files).hasSize(2)
        assertExtFile(extFilesTable.files.first(), testFile)
        assertExtFile(extFilesTable.files.second(), testFile)
    }

    @Test
    fun `to ext files`() {
        val docFilesTable = DocFileTable(listOf(testNfsDocFile, testFireDocFile))
        val docFiles = listOf(left(testNfsDocFile), left(testNfsDocFile), right(docFilesTable))
        val extFiles = docFiles.map { it.toExtFiles() }

        assertThat(extFiles).hasSize(3)
        extFiles.first().ifLeft { assertExtFile(it, testFile) }
        extFiles.second().ifLeft { assertExtFile(it, testFile) }
        extFiles.third().ifRight {
            assertThat(it.files).hasSize(2)
            assertExtFile(it.files.first(), testFile)
            assertExtFile(it.files.second(), testFile)
        }
    }

    @Test
    fun `to ext file list`() {
        assertExtFileList(docFileList.toExtFileList())
    }

    @Test
    fun `file list file to ext file`() {
        val id = ObjectId()
        val submissionId = ObjectId()
        val fileListDocFile = FileListDocFile(
            id,
            submissionId,
            TEST_REL_PATH,
            testFile.absolutePath,
            listOf(basicDocAttribute),
            "test-md5",
            NFS
        )

        val extFile = fileListDocFile.toExtFile() as NfsFile
        assertThat(extFile.fileName).isEqualTo(TEST_REL_PATH)
        assertThat(extFile.file).isEqualTo(testFile)
        assertThat(extFile.attributes).containsExactly(basicDocAttribute.toExtAttribute())
        assertThat(extFile.md5).isEqualTo("test-md5")
    }
}
