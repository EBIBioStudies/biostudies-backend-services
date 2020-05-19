package ac.uk.ebi.biostd.persistence.service.filesystem

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.ext.asFileList
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.clean
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
internal class FtpFilesServiceTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val submission: ExtSubmission
) {

    private lateinit var expectedDirectory: File
    private lateinit var expectedFile1: File
    private lateinit var expectedFile2: File

    private val folderResolver = SubmissionFolderResolver(temporaryFolder.root.toPath())
    private val testInstance = FtpFilesService(folderResolver)

    @BeforeAll
    fun beforeAll() {
        every { submission.relPath } returns "My/Path/To/Submission"
    }

    @BeforeEach
    fun beforeEach() {
        temporaryFolder.clean()

        val submissionFolder = folderResolver.getSubmissionFolder(submission).toFile().apply { mkdirs() }
        expectedDirectory = submissionFolder.createDirectory("my-directory")
        expectedFile1 = expectedDirectory.createNewFile("file.txt", "file-content")
        expectedFile2 = expectedDirectory.createNewFile("file-2.txt", "file-text")
    }

    @Test
    fun createFtpFolder() {
        testInstance.createFtpFolder(submission)

        assertFolder(folderResolver.getSubmissionFolder(submission).toFile())
        assertFolder(folderResolver.getSubmissionFtpFolder(submission).toFile())
    }

    @Test
    fun cleanFtpFolder() {
        testInstance.createFtpFolder(submission)
        testInstance.cleanFtpFolder(submission)

        assertFolder(folderResolver.getSubmissionFolder(submission).toFile())
        assertThat(folderResolver.getSubmissionFtpFolder(submission).toFile()).doesNotExist()
    }

    private fun assertFolder(ftpFolder: File) {
        val directory = ftpFolder.asFileList().first()
        assertThat(directory).hasName(expectedDirectory.name)
        assertThat(directory).isDirectory()

        val files = directory.asFileList().sortedBy { it.name }
        assertFile(files.first(), expectedFile2.name, expectedFile2.readText())
        assertFile(files.second(), expectedFile1.name, expectedFile1.readText())
    }

    private fun assertFile(file: File, name: String, content: String) {
        assertThat(file).hasName(name)
        assertThat(file).hasContent(content)
    }
}
