package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.assertExtFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.assertExtFileList
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileList
import ac.uk.ebi.biostd.persistence.doc.test.TEST_PATH
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class ToExtFileTest(
    temporaryFolder: TemporaryFolder,
    @MockK private val filesSource: FilesSource
) {
    private val testFile = temporaryFolder.createFile(TEST_PATH)

    @BeforeEach
    fun beforeEach() {
        every { filesSource.getFile(TEST_PATH) } returns testFile
    }

    @Test
    fun `to ext file`() {
        val extFile = docFile.toExtFile(filesSource)
        assertExtFile(extFile, testFile)
    }

    @Test
    fun `to ext file table`() {
        val docFilesTable = DocFileTable(listOf(docFile))
        val extFilesTable = docFilesTable.toExtFileTable(filesSource)

        assertThat(extFilesTable.files).hasSize(1)
        assertExtFile(extFilesTable.files.first(), testFile)
    }

    @Test
    fun `to ext files`() {
        val docFilesTable = DocFileTable(listOf(docFile))
        val docFiles = listOf(left(docFile), right(docFilesTable))
        val extFiles = docFiles.map { it.toExtFiles(filesSource) }

        assertThat(extFiles).hasSize(2)
        extFiles.first().ifLeft { assertExtFile(it, testFile) }
        extFiles.second().ifRight {
            assertThat(it.files).hasSize(1)
            assertExtFile(it.files.first(), testFile)
        }
    }

    @Test
    fun `to ext file list`() {
        val extFileList = docFileList.toExtFileList(filesSource)
        assertExtFileList(extFileList, testFile)
    }
}
