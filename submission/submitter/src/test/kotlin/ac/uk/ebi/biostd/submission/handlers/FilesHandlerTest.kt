package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.SubFormat.JSON
import ac.uk.ebi.biostd.submission.model.PathFilesSource
import ac.uk.ebi.biostd.submission.test.ACC_NO
import ac.uk.ebi.biostd.submission.test.USER_ID
import ac.uk.ebi.biostd.submission.test.USER_SECRET_KEY
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.paths.FolderResolver
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
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class FilesHandlerTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val mockFilesCopier: FilesCopier,
    @MockK private val mockFolderResolver: FolderResolver,
    @MockK private val mockFilesValidator: FilesValidator,
    @MockK private val mockLibraryFilesHandler: LibraryFilesHandler,
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
        testInstance =
            FilesHandler(
                mockFolderResolver,
                mockFilesValidator,
                mockFilesCopier,
                mockLibraryFilesHandler,
                mockOutputFilesGenerator)

        initMocks()
    }

    // TODO add unit tests for the individual processors
    @Test
    fun `process submission files`() {
        testInstance.processFiles(submission, emptyList(), JSON)

        verify(exactly = 1) { mockFilesCopier.copy(submission, any<PathFilesSource>()) }
        verify(exactly = 1) { mockFilesValidator.validate(submission, any<PathFilesSource>()) }
        verify(exactly = 1) { mockOutputFilesGenerator.generate(submission) }
        verify(exactly = 1) { mockLibraryFilesHandler.processLibraryFiles(submission, any<PathFilesSource>(), JSON) }
    }

    private fun initMocks() {
        every { mockFilesCopier.copy(submission, any<PathFilesSource>()) } answers { nothing }
        every { mockFilesValidator.validate(submission, any<PathFilesSource>()) } answers { nothing }
        every { mockOutputFilesGenerator.generate(submission) } answers { nothing }

        every {
            mockLibraryFilesHandler.processLibraryFiles(submission, any<PathFilesSource>(), JSON)
        } answers { nothing }

        every {
            mockFolderResolver.getUserMagicFolderPath(USER_ID, USER_SECRET_KEY)
        } returns Paths.get("${temporaryFolder.root.absolutePath}/$USER_SECRET_KEY")
    }
}
