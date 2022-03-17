package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.assertExtFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.fireDocDirectory
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.fireDocFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.nfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.TEST_FILENAME
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ToExtFileTest(temporaryFolder: TemporaryFolder) {
    private val testFile = temporaryFolder.createFile(TEST_FILENAME)
    private val testNfsDocFile = nfsDocFile.copy(
        fullPath = testFile.absolutePath,
        md5 = testFile.md5(),
        fileSize = testFile.size()
    )

    @Test
    fun `nfsDocFile to ext file`() {
        val extFile = testNfsDocFile.toExtFile()
        assertExtFile(extFile, testFile)
    }

    @Test
    fun `fireDocFile to ext file`() {
        val extFile = fireDocFile.toExtFile()
        assertExtFile(extFile, testFile)
    }

    @Test
    fun `fire directory to ext file`() {
        val extFile = fireDocDirectory.toExtFile()
        assertExtFile(extFile, testFile)
    }

    @Test
    fun `to ext file table`() {
        val docFilesTable = DocFileTable(listOf(testNfsDocFile, fireDocFile))
        val extFilesTable = docFilesTable.toExtFileTable()

        assertThat(extFilesTable.files).hasSize(2)
        assertExtFile(extFilesTable.files.first(), testFile)
        assertExtFile(extFilesTable.files.second(), testFile)
    }

    @Test
    fun `to ext files`() {
        val docFilesTable = DocFileTable(listOf(testNfsDocFile, fireDocFile))
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
}
