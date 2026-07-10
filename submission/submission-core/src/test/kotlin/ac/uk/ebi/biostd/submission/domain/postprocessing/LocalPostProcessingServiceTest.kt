package ac.uk.ebi.biostd.submission.domain.postprocessing

import ac.uk.ebi.biostd.common.properties.CleanUpProperties
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionFilesDocDataRepository
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.submission.service.DoiService
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.FileSourceType
import ebi.ac.uk.extended.model.FileSourceType.SUBMISSION
import ebi.ac.uk.extended.model.FileSourceType.USER
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.PersistedExtFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.nio.file.Files
import java.nio.file.Path

@ExtendWith(MockKExtension::class)
class LocalPostProcessingServiceTest(
    @param:MockK private val pageTabService: PageTabService,
    @param:MockK private val statsDataService: StatsDataService,
    @param:MockK private val fileStorageService: FileStorageService,
    @param:MockK private val subFolderResolver: SubmissionFolderResolver,
    @param:MockK private val serializationService: ExtSerializationService,
    @param:MockK private val extSubQueryService: SubmissionPersistenceQueryService,
    @param:MockK private val requestFilesService: SubmissionRequestFilesPersistenceService,
    @param:MockK private val submissionFileRepository: SubmissionFilesDocDataRepository,
    @param:MockK private val toSimpleSubmissionMapper: ToSubmissionMapper,
    @param:MockK private val cleanUpProperties: CleanUpProperties,
    @param:MockK private val doiService: DoiService,
) {
    private val testInstance =
        LocalPostProcessingService(
            pageTabService = pageTabService,
            statsDataService = statsDataService,
            fileStorageService = fileStorageService,
            subFolderResolver = subFolderResolver,
            serializationService = serializationService,
            extSubQueryService = extSubQueryService,
            requestFilesService = requestFilesService,
            submissionFileRepository = submissionFileRepository,
            toSimpleSubmissionMapper = toSimpleSubmissionMapper,
            cleanUpProperties = cleanUpProperties,
            doiService = doiService,
        )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { cleanUpProperties.concurrency } returns 10
        every { cleanUpProperties.postProcessCleanUpEnabled } returns true
    }

    @Test
    fun `clean up files does nothing when post process clean up is disabled`() =
        runTest {
            every { cleanUpProperties.postProcessCleanUpEnabled } returns false

            testInstance.cleanUpFiles("S-BSST1")

            verify {
                listOf(
                    pageTabService,
                    statsDataService,
                    fileStorageService,
                    subFolderResolver,
                    serializationService,
                    extSubQueryService,
                    requestFilesService,
                    submissionFileRepository,
                    toSimpleSubmissionMapper,
                    doiService,
                ) wasNot Called
            }
        }

    @Test
    fun `clean up files deletes only matching user nfs source files when post process clean up is enabled`() =
        runTest {
            val sub = basicExtSubmission.copy(accNo = "S-BSST1", version = 3)
            val matchingUserFile = tempFile("matching-user-source", "same")
            val mismatchingUserFile = tempFile("mismatching-user-source", "source")
            val subSourceFile = tempFile("submission-source", "same")
            val nullSourceTypeFile = tempFile("null-source-type", "same")
            val userDirectory = Files.createTempDirectory("user-source-directory")
            val mismatchingRequestFile =
                requestFile(
                    sub.accNo,
                    sub.version,
                    "Files/mismatching.txt",
                    nfsSource(mismatchingUserFile),
                    USER,
                    targetMd5 = "different-md5",
                )
            val matchingRequestFile =
                requestFile(sub.accNo, sub.version, "Files/matching.txt", nfsSource(matchingUserFile), USER)
            val submissionSourceRequestFile =
                requestFile(sub.accNo, sub.version, "Files/submission.txt", nfsSource(subSourceFile), SUBMISSION)
            val nullSourceTypeRequestFile =
                requestFile(sub.accNo, sub.version, "Files/null-source-type.txt", nfsSource(nullSourceTypeFile), null)
            val fireSourceRequestFile =
                requestFile(sub.accNo, sub.version, "Files/fire-source.txt", fireSource(), USER)
            val directorySourceRequestFile =
                requestFile(sub.accNo, sub.version, "Files/directory-source.txt", nfsSource(userDirectory), USER)

            coEvery {
                extSubQueryService.getExtByAccNo(sub.accNo, includeFileListFiles = false, includeLinkListLinks = false)
            } returns sub
            every { requestFilesService.getSubmissionRequestFiles(sub.accNo, sub.version) } returns
                flowOf(
                    matchingRequestFile,
                    mismatchingRequestFile,
                    submissionSourceRequestFile,
                    nullSourceTypeRequestFile,
                    fireSourceRequestFile,
                    directorySourceRequestFile,
                )

            testInstance.cleanUpFiles(sub.accNo)

            assertThat(matchingUserFile).doesNotExist()
            assertThat(mismatchingUserFile).exists()
            assertThat(subSourceFile).exists()
            assertThat(nullSourceTypeFile).exists()
            assertThat(userDirectory).exists()
            coVerify(exactly = 1) {
                extSubQueryService.getExtByAccNo(sub.accNo, includeFileListFiles = false, includeLinkListLinks = false)
            }
            verify(exactly = 1) { requestFilesService.getSubmissionRequestFiles(sub.accNo, sub.version) }
            verify {
                listOf(
                    pageTabService,
                    statsDataService,
                    fileStorageService,
                    subFolderResolver,
                    serializationService,
                    submissionFileRepository,
                    toSimpleSubmissionMapper,
                    doiService,
                ) wasNot Called
            }
        }

    private fun tempFile(
        prefix: String,
        content: String,
    ): Path = Files.createTempFile(prefix, ".txt").also { Files.writeString(it, content) }

    private fun requestFile(
        accNo: String,
        version: Int,
        path: String,
        sourceFile: ExtFile,
        sourceType: FileSourceType?,
        targetMd5: String = (sourceFile as? PersistedExtFile)?.md5.orEmpty(),
    ) = SubmissionRequestFile(
        accNo = accNo,
        version = version,
        path = path,
        file = targetFile(path, targetMd5),
        sourceFile = sourceFile,
        status = INDEXED,
        sourceType = sourceType,
    )

    private fun nfsSource(path: Path): NfsFile {
        val file = path.toFile()
        return NfsFile(
            filePath = path.fileName.toString(),
            relPath = path.fileName.toString(),
            file = file,
            fullPath = file.absolutePath,
            md5 = if (file.isFile) file.md5() else "",
            size = if (file.isFile) file.length() else 0L,
            type = if (file.isFile) FILE else DIR,
            sourceType = USER,
        )
    }

    private fun fireSource(): FireFile {
        val path = "Files/fire-source.txt"
        return FireFile(
            fireId = "fire-id-$path",
            firePath = path,
            published = true,
            filePath = path,
            relPath = path,
            md5 = "fire-md5",
            size = 1L,
            type = FILE,
            sourceType = USER,
        )
    }

    private fun targetFile(
        path: String,
        md5: String,
    ): NfsFile =
        NfsFile(
            filePath = path,
            relPath = path,
            file = Files.createTempFile("target-file", ".txt").toFile(),
            fullPath = path,
            md5 = md5,
            size = 1L,
            type = FILE,
        )
}
