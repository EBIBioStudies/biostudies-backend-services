package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ac.uk.ebi.biostd.submission.test.ACC_NO
import ac.uk.ebi.biostd.submission.test.USER_ID
import ac.uk.ebi.biostd.submission.test.USER_SECRET_KEY
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.paths.FolderResolver
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.nio.file.Paths
import ebi.ac.uk.model.File as SubmissionFile

const val TEST_FILE_1 = "file.txt"
const val TEST_FILE_2 = "file2.txt"
const val TEST_FOLDER = "folder1"
const val NON_EXISTING_FILE = "GhostFile.txt"
const val JSON_SUBMISSION = "{ \"accNo\": \"$ACC_NO\" }"
const val XML_SUBMISSION = "<submission accNo=\"$ACC_NO\"></submission>"
const val TSV_SUBMISSION = "Submission\t$ACC_NO"

@TestInstance(PER_CLASS)
@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class FilesHandlerTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val mockFolderResolver: FolderResolver,
    @MockK private val mockSerializationService: SerializationService
) {
    private lateinit var submission: ExtendedSubmission
    private lateinit var testInstance: FilesHandler

    private val submissionFolderPath: String = "${temporaryFolder.root.absolutePath}/$ACC_NO"
    private val testSubFile1Path: String = "$submissionFolderPath/$TEST_FILE_1"
    private val testSubFile2Path: String = "$submissionFolderPath/$TEST_FOLDER/$TEST_FILE_2"

    @BeforeAll
    fun beforeAll() {
        temporaryFolder.createDirectory(ACC_NO)
        temporaryFolder.createDirectory(USER_SECRET_KEY)
        temporaryFolder.createDirectory("$USER_SECRET_KEY/$TEST_FOLDER")
        temporaryFolder.createFile("$USER_SECRET_KEY/$TEST_FILE_1")
        temporaryFolder.createFile("$USER_SECRET_KEY/$TEST_FOLDER/$TEST_FILE_2")
    }

    @BeforeEach
    fun beforeEach() {
        submission = createBasicExtendedSubmission()
        testInstance = FilesHandler(mockFolderResolver, mockSerializationService)

        initMockFileResolver()
        initMockSerializationService()
        initTestSubmissionFiles()
    }

    @Test
    fun `process submission files`() {
        testInstance.processFiles(submission)

        assertSubmissionFile("$ACC_NO.tsv", TSV_SUBMISSION)
        assertSubmissionFile("$ACC_NO.xml", XML_SUBMISSION)
        assertSubmissionFile("$ACC_NO.json", JSON_SUBMISSION)

        assertSubmissionFile(TEST_FILE_1, "")
        assertSubmissionFile("$TEST_FOLDER/$TEST_FILE_2", "")
    }

    @Test
    fun `process submission with invalid files`() {
        val nonExistingFile = SubmissionFile(NON_EXISTING_FILE)
        submission.section.addFile(nonExistingFile)

        val exception = catchThrowable { testInstance.processFiles(submission) }
        assertThat(exception).isInstanceOf(InvalidFilesException::class.java)
        assertThat(exception).hasMessage(INVALID_FILES_ERROR_MSG)
        assertThat((exception as InvalidFilesException).invalidFiles).hasSize(1).contains(nonExistingFile)
    }

    private fun assertSubmissionFile(name: String, expectedContent: String) {
        val file = File("$submissionFolderPath/$name")

        assertThat(file.exists()).isTrue()
        assertThat(file.readBytes()).isEqualTo(expectedContent.toByteArray())
    }

    private fun initMockFileResolver() {
        every { mockFolderResolver.getSubmissionFolder(submission) } returns Paths.get(submissionFolderPath)

        every {
            mockFolderResolver.getSubFilePath(submissionFolderPath, TEST_FILE_1)
        } returns Paths.get(testSubFile1Path)

        every {
            mockFolderResolver.getSubFilePath(submissionFolderPath, "$TEST_FOLDER/$TEST_FILE_2")
        } returns Paths.get(testSubFile2Path)

        every {
            mockFolderResolver.getUserMagicFolderPath(USER_ID, USER_SECRET_KEY)
        } returns Paths.get("${temporaryFolder.root.absolutePath}/$USER_SECRET_KEY")
    }

    private fun initMockSerializationService() {
        every { mockSerializationService.serializeSubmission(submission, SubFormat.JSON) } returns JSON_SUBMISSION
        every { mockSerializationService.serializeSubmission(submission, SubFormat.XML) } returns XML_SUBMISSION
        every { mockSerializationService.serializeSubmission(submission, SubFormat.TSV) } returns TSV_SUBMISSION
    }

    private fun initTestSubmissionFiles() {
        val section = Section()
        section.addFile(SubmissionFile(TEST_FILE_1))
        section.addFile(SubmissionFile("$TEST_FOLDER/$TEST_FILE_2"))

        submission.section = section
        submission.relPath = submissionFolderPath
        submission.rootPath = submissionFolderPath
    }
}
