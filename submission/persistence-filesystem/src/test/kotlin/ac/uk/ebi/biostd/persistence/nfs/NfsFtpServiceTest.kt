package ac.uk.ebi.biostd.persistence.nfs

import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.io.ext.asFileList
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.nio.file.Files

private const val REL_PATH = "My/Path/To/Submission"

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
internal class NfsFtpServiceTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val extSubmission: ExtSubmission
) {
    private lateinit var expectedDirectory: File
    private lateinit var expectedFile1: File
    private lateinit var expectedFile2: File

    private val folderResolver = SubmissionFolderResolver(
        temporaryFolder.root.toPath().resolve("submission"),
        temporaryFolder.root.toPath().resolve("ftp")
    )
    private val testInstance = NfsFtpService(folderResolver)

    @BeforeEach
    fun beforeEach() {
        temporaryFolder.clean()

        val submissionFolder = submissionFolder()
        expectedDirectory = createFolder(submissionFolder.resolve("my-directory"))
        expectedFile1 = expectedDirectory.createNewFile("file.txt", "file-content")
        expectedFile2 = expectedDirectory.createNewFile("file-2.txt", "file-text")

        every { extSubmission.relPath } returns REL_PATH
    }

    private fun submissionFolder(): File {
        val submissionFolder = folderResolver.getSubFolder(REL_PATH).toFile()
        return createFolder(submissionFolder)
    }

    private fun createFolder(file: File): File {
        FileUtils.createEmptyFolder(file.toPath(), RWXR_XR_X)
        return file
    }

    @Test
    fun `create ftp folder`() {
        testInstance.createFtpFolder(REL_PATH)

        assertFolder(folderResolver.getSubmissionFtpFolder(REL_PATH).toFile())
    }

    @Test
    fun `process public submission`() {
        every { extSubmission.released } returns true

        testInstance.processSubmissionFiles(extSubmission)

        assertFolder(folderResolver.getSubmissionFtpFolder(REL_PATH).toFile())
    }

    @Test
    fun `process private submission`() {
        every { extSubmission.released } returns false

        testInstance.processSubmissionFiles(extSubmission)

        assertThat(folderResolver.getSubmissionFtpFolder(REL_PATH).toFile()).doesNotExist()
    }

    private fun assertFolder(ftpFolder: File) {
        val directory = ftpFolder.asFileList().first()
        assertThat(directory).hasName(expectedDirectory.name)
        assertThat(directory).isDirectory()
        assertThat(Files.getPosixFilePermissions(directory.toPath())).hasSameElementsAs(RWXR_XR_X)

        val files = directory.asFileList().sortedBy { it.name }
        assertFile(files.first(), expectedFile2.name, expectedFile2.readText())
        assertFile(files.second(), expectedFile1.name, expectedFile1.readText())
    }

    private fun assertFile(file: File, name: String, content: String) {
        assertThat(file).hasName(name)
        assertThat(file).hasContent(content)
        assertThat(Files.getPosixFilePermissions(file.toPath())).hasSameElementsAs(RW_R__R__)
    }
}
