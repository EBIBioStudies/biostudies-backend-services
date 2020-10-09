package ac.uk.ebi.biostd.persistence.service.filesystem

import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.io.ext.asFileList
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.clean
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.nio.file.Files

private const val REL_PATH = "My/Path/To/Submission"

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
internal class FtpFilesServiceTest(private val temporaryFolder: TemporaryFolder) {
    private lateinit var expectedDirectory: File
    private lateinit var expectedFile1: File
    private lateinit var expectedFile2: File

    private val folderResolver = SubmissionFolderResolver(
        temporaryFolder.root.toPath().resolve("submission"),
        temporaryFolder.root.toPath().resolve("ftp"))
    private val testInstance = FtpFilesService(folderResolver)

    @BeforeEach
    fun beforeEach() {
        temporaryFolder.clean()

        val submissionFolder = folderResolver.getSubFolder(REL_PATH).toFile().apply { mkdirs() }
        expectedDirectory = submissionFolder.createDirectory("my-directory")
        expectedFile1 = expectedDirectory.createNewFile("file.txt", "file-content")
        expectedFile2 = expectedDirectory.createNewFile("file-2.txt", "file-text")
    }

    @Test
    fun createFtpFolder() {
        testInstance.createFtpFolder(REL_PATH)

        assertFolder(folderResolver.getSubFolder(REL_PATH).toFile())
        assertFolder(folderResolver.getSubmissionFtpFolder(REL_PATH).toFile())
    }

    @Test
    fun cleanFtpFolder() {
        testInstance.createFtpFolder(REL_PATH)
        testInstance.cleanFtpFolder(REL_PATH)

        assertFolder(folderResolver.getSubFolder(REL_PATH).toFile())
        assertThat(folderResolver.getSubmissionFtpFolder(REL_PATH).toFile()).doesNotExist()
    }

    private fun assertFolder(ftpFolder: File) {
        val directory = ftpFolder.asFileList().first()
        assertThat(directory).hasName(expectedDirectory.name)
        assertThat(directory).isDirectory()
        assertThat(Files.getPosixFilePermissions(directory.toPath())).isEqualTo(RWXR_XR_X)

        val files = directory.asFileList().sortedBy { it.name }
        assertFile(files.first(), expectedFile2.name, expectedFile2.readText())
        assertFile(files.second(), expectedFile1.name, expectedFile1.readText())
    }

    private fun assertFile(file: File, name: String, content: String) {
        assertThat(file).hasName(name)
        assertThat(file).hasContent(content)
        assertThat(Files.getPosixFilePermissions(file.toPath())).isEqualTo(RW_R__R__)
    }
}
