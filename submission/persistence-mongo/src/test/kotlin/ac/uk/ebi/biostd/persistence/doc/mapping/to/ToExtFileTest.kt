package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.assertExtFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.assertFireDirectory
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.fireDocDirectory
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.fireDocFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.nfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.TEST_FILENAME
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.base.Either.Companion.left
import uk.ac.ebi.biostd.base.Either.Companion.right
import uk.ac.ebi.biostd.extended.model.FireFile
import uk.ac.ebi.biostd.io.ext.md5
import uk.ac.ebi.biostd.io.ext.size
import uk.ac.ebi.biostd.util.collections.ifLeft
import uk.ac.ebi.biostd.util.collections.ifRight
import uk.ac.ebi.biostd.util.collections.second
import uk.ac.ebi.biostd.util.collections.third

@ExtendWith(TemporaryFolderExtension::class)
class ToExtFileTest(temporaryFolder: TemporaryFolder) {
    private val testFile = temporaryFolder.createFile(TEST_FILENAME)
    private val testNfsDocFile =
        nfsDocFile.copy(
            fullPath = testFile.absolutePath,
            md5 = testFile.md5(),
            fileSize = testFile.size(),
        )

    @Test
    fun `nfsDocFile to ext file`() {
        val extFile = testNfsDocFile.toExtFile(false, SUB_REL_PATH)
        assertExtFile(extFile, testFile)
    }

    @Test
    fun `fireDocFile to ext file`() {
        val extFile = fireDocFile.toExtFile(false, SUB_REL_PATH)
        assertExtFile(extFile, testFile)
    }

    @Test
    fun `fire directory to ext file`() {
        val extFile = fireDocDirectory.toExtFile(false, SUB_REL_PATH) as FireFile
        assertFireDirectory(extFile)
    }

    @Test
    fun `to ext file table`() {
        val docFilesTable = DocFileTable(listOf(testNfsDocFile, fireDocFile))
        val extFilesTable = docFilesTable.toExtFileTable(false, SUB_REL_PATH)

        assertThat(extFilesTable.files).hasSize(2)
        assertExtFile(extFilesTable.files.first(), testFile)
        assertExtFile(extFilesTable.files.second(), testFile)
    }

    @Test
    fun `to ext files`() {
        val docFilesTable = DocFileTable(listOf(testNfsDocFile, fireDocFile))
        val docFiles = listOf(left(testNfsDocFile), left(testNfsDocFile), right(docFilesTable))
        val extFiles = docFiles.map { it.toExtFiles(false, SUB_REL_PATH) }

        assertThat(extFiles).hasSize(3)
        extFiles.first().ifLeft { assertExtFile(it, testFile) }
        extFiles.second().ifLeft { assertExtFile(it, testFile) }
        extFiles.third().ifRight {
            assertThat(it.files).hasSize(2)
            assertExtFile(it.files.first(), testFile)
            assertExtFile(it.files.second(), testFile)
        }
    }

    companion object {
        private const val SUB_REL_PATH = "SubRelPath"
    }
}
