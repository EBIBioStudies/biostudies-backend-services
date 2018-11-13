package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.File as SubmissionFile
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.User
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.paths.FolderResolver
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

const val USER_ID = 123L
const val ACC_NO = "ABC456"
const val TEST_FILE = "file.txt"
const val USER_EMAIL = "user@mail.com"
const val USER_SECRET_KEY = "SecretKey"
const val JSON_SUBMISSION = "{ \"accNo\": \"$ACC_NO\" }"
const val XML_SUBMISSION = "<submission accNo=\"$ACC_NO\"></submission>"
const val TSV_SUBMISSION = "Submission\t$ACC_NO"

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class FilesHandlerTest(
        private val temporaryFolder: TemporaryFolder,
        @MockK private val mockFolderResolver: FolderResolver,
        @MockK private val mockSerializationService: SerializationService
) {

    private val user = User(USER_ID, USER_EMAIL, USER_SECRET_KEY)
    private val submission = ExtendedSubmission(ACC_NO, user)

    private lateinit var testSubFilePath: String
    private lateinit var submissionFolderPath: String

    private lateinit var testInstance: FilesHandler

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        initMockFileResolver()
        initMockSerializationService()
        initTestSubmissionFiles()

        testInstance = FilesHandler(mockFolderResolver, mockSerializationService)
    }

    @Test
    fun processFiles() {
        testInstance.processFiles(user, submission)

        assertSubmissionFile("$ACC_NO.tsv", TSV_SUBMISSION)
        assertSubmissionFile("$ACC_NO.xml", XML_SUBMISSION)
        assertSubmissionFile("$ACC_NO.json", JSON_SUBMISSION)

        assertThat(Files.exists(Paths.get(testSubFilePath))).isTrue()
    }

    private fun assertSubmissionFile(name: String, expectedContent: String) {
        val file = File("$submissionFolderPath/$name")

        assertThat(file.exists()).isTrue()
        assertThat(file.readBytes()).isEqualTo(expectedContent.toByteArray())
    }

    private fun initMockFileResolver() {
        temporaryFolder.createDirectory(ACC_NO)
        temporaryFolder.createDirectory(USER_SECRET_KEY)
        temporaryFolder.createFile("$USER_SECRET_KEY/$TEST_FILE")

        val userFolderPath = "${temporaryFolder.root.absolutePath}/$USER_SECRET_KEY"
        submissionFolderPath = "${temporaryFolder.root.absolutePath}/$ACC_NO"
        testSubFilePath = "$submissionFolderPath/$TEST_FILE"

        every { mockFolderResolver.getSubmissionFolder(submission) } returns Paths.get(submissionFolderPath)
        every { mockFolderResolver.getSubFilePath(submissionFolderPath, TEST_FILE) } returns Paths.get(testSubFilePath)
        every { mockFolderResolver.getUserMagicFolderPath(USER_ID, USER_SECRET_KEY) } returns Paths.get(userFolderPath)
    }

    private fun initMockSerializationService() {
        every { mockSerializationService.serializeSubmission(submission, SubFormat.JSON) } returns JSON_SUBMISSION
        every { mockSerializationService.serializeSubmission(submission, SubFormat.XML) } returns XML_SUBMISSION
        every { mockSerializationService.serializeSubmission(submission, SubFormat.TSV) } returns TSV_SUBMISSION
    }

    private fun initTestSubmissionFiles() {
        val section = Section()
        section.addFile(SubmissionFile(TEST_FILE))

        submission.rootSection = section
        submission.relPath = submissionFolderPath
        submission.rootPath = submissionFolderPath
    }
}
