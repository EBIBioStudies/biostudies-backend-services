package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.assertExtFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.assertExtFileList
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileList
import ac.uk.ebi.biostd.persistence.doc.test.TEST_REL_PATH
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ToExtFileTest(temporaryFolder: TemporaryFolder) {
    private val testFile = temporaryFolder.createFile(TEST_REL_PATH)
    private val testDocFile = docFile.copy(fullPath = testFile.absolutePath)

    @Test
    fun `to ext file`() {
        val extFile = testDocFile.toExtFile()
        assertExtFile(extFile, testFile)
    }

    @Test
    fun `to ext file table`() {
        val docFilesTable = DocFileTable(listOf(testDocFile))
        val extFilesTable = docFilesTable.toExtFileTable()

        assertThat(extFilesTable.files).hasSize(1)
        assertExtFile(extFilesTable.files.first(), testFile)
    }

    @Test
    fun `to ext files`() {
        val docFilesTable = DocFileTable(listOf(testDocFile))
        val docFiles = listOf(left(testDocFile), right(docFilesTable))
        val extFiles = docFiles.map { it.toExtFiles() }

        assertThat(extFiles).hasSize(2)
        extFiles.first().ifLeft { assertExtFile(it, testFile) }
        extFiles.second().ifRight {
            assertThat(it.files).hasSize(1)
            assertExtFile(it.files.first(), testFile)
        }
    }

    @Test
    fun `to ext file list`() {
        val extFileList = docFileList.copy(files = listOf(testDocFile)).toExtFileList()
        assertExtFileList(extFileList, testFile)
    }
}
