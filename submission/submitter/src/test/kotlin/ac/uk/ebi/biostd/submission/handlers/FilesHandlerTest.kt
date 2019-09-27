package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.submission.test.ACC_NO
import ac.uk.ebi.biostd.submission.test.USER_SECRET_KEY
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class FilesHandlerTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val mockFilesCopier: FilesCopier,
    @MockK private val mockFilesValidator: FilesValidator,
    @MockK private val mockOutputFilesGenerator: OutputFilesGenerator
) {
    private lateinit var submission: ExtendedSubmission
    private lateinit var testInstance: FilesHandler

    @BeforeAll
    fun beforeAll() {
        temporaryFolder.createDirectory(ACC_NO)
        temporaryFolder.createDirectory(USER_SECRET_KEY)
    }

    @BeforeEach
    fun beforeEach() {
        submission = createBasicExtendedSubmission()
        testInstance = FilesHandler(mockFilesValidator, mockFilesCopier, mockOutputFilesGenerator)

        initMocks()
    }

    // TODO add unit tests for the individual processors
    @Test
    fun `process submission files`(@MockK userSource: FilesSource) {
        testInstance.processFiles(submission, userSource)

        verify(exactly = 1) { mockFilesCopier.copy(submission, userSource) }
        verify(exactly = 1) { mockFilesValidator.validate(submission, userSource) }
        verify(exactly = 1) { mockOutputFilesGenerator.generate(submission) }
    }

    private fun initMocks() {
        every { mockFilesCopier.copy(submission, any()) } answers { nothing }
        every { mockFilesValidator.validate(submission, any()) } answers { nothing }
        every { mockOutputFilesGenerator.generate(submission) } answers { nothing }
    }
}
