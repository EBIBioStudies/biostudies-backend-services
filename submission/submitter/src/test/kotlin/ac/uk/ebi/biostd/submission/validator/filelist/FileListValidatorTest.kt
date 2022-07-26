package ac.uk.ebi.biostd.submission.validator.filelist

import ac.uk.ebi.biostd.common.SerializationConfig
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.service.FileSourcesRequest
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ebi.ac.uk.dsl.excel.excel
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.io.sources.PathSource
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FileListValidatorTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val fileSourcesService: FileSourcesService,
    @MockK private val submissionQueryService: SubmissionPersistenceQueryService,
) {
    private val serializationService = SerializationConfig.serializationService()
    private val filesSource = FileSourcesList(listOf(PathSource("Description", tempFolder.root.toPath())))
    private val testInstance = FileListValidator(fileSourcesService, serializationService, submissionQueryService)

    @BeforeAll
    fun beforeAll() {
        tempFolder.createFile("ref.txt")
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `valid file list`(
        @MockK submitter: SecurityUser,
        @MockK onBehalfUser: SecurityUser,
        @MockK extSubmission: ExtSubmission,
    ) {
        val fileSourcesSlot = slot<FileSourcesRequest>()
        every { submissionQueryService.findExtByAccNo("S-BSST0") } returns extSubmission
        every { fileSourcesService.submissionSources(capture(fileSourcesSlot)) } returns filesSource

        val content = tsv {
            line("Files", "Type")
            line("ref.txt", "test")
        }
        tempFolder.createFile("valid.tsv", content.toString())

        val request = FileListValidationRequest("S-BSST0", "root-path", "valid.tsv", submitter, onBehalfUser)
        testInstance.validateFileList(request)

        val fileSourceRequest = fileSourcesSlot.captured
        assertThat(fileSourceRequest.files).isNull()
        assertThat(fileSourceRequest.preferredSources).isEmpty()
        assertThat(fileSourceRequest.submitter).isEqualTo(submitter)
        assertThat(fileSourceRequest.rootPath).isEqualTo("root-path")
        assertThat(fileSourceRequest.submission).isEqualTo(extSubmission)
        assertThat(fileSourceRequest.onBehalfUser).isEqualTo(onBehalfUser)

        verify(exactly = 1) {
            submissionQueryService.findExtByAccNo("S-BSST0")
            fileSourcesService.submissionSources(fileSourceRequest)
        }
    }

    @Test
    fun `invalid file list`(
        @MockK submitter: SecurityUser,
        @MockK extSubmission: ExtSubmission,
    ) {
        val fileSourcesSlot = slot<FileSourcesRequest>()
        every { submissionQueryService.findExtByAccNo("S-BSST0") } returns extSubmission
        every { fileSourcesService.submissionSources(capture(fileSourcesSlot)) } returns filesSource

        excel(tempFolder.createFile("fail.xlsx")) {
            sheet("page tab") {
                row {
                    cell("Files")
                    cell("Type")
                }
                row {
                    cell("ref.txt")
                    cell("test")
                }
                row {
                    cell("ghost.txt")
                    cell("fail")
                }
            }
        }

        val request = FileListValidationRequest(null, null, "fail.xlsx", submitter, null)
        assertThrows<FilesProcessingException> { testInstance.validateFileList(request) }

        val fileSourceRequest = fileSourcesSlot.captured
        assertThat(fileSourceRequest.files).isNull()
        assertThat(fileSourceRequest.rootPath).isNull()
        assertThat(fileSourceRequest.submission).isNull()
        assertThat(fileSourceRequest.onBehalfUser).isNull()
        assertThat(fileSourceRequest.preferredSources).isEmpty()
        assertThat(fileSourceRequest.submitter).isEqualTo(submitter)

        verify(exactly = 0) { submissionQueryService.findExtByAccNo("S-BSST0") }
        verify(exactly = 1) { fileSourcesService.submissionSources(fileSourceRequest) }
    }
}
