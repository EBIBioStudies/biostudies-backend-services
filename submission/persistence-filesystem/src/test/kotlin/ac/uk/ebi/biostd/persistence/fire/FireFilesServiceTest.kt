package ac.uk.ebi.biostd.persistence.fire

import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.test.basicExtSubmission
import ebi.ac.uk.util.collections.ifLeft
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.FireFile as ClientFireFile

@Disabled
@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FireFilesServiceTest(
    tempFolder: TemporaryFolder,
    @MockK private val fireWebClient: FireWebClient
) {
    private val file = tempFolder.createFile("test.txt")
    private val testMd5 = file.md5()
    private val attribute = ExtAttribute("Type", "Test")
    private val testInstance = FireFilesService(fireWebClient)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { fireWebClient.save(file, testMd5, "", "") } returns ClientFireFile(1, "abc1", testMd5, 1, "2021-07-08")
        every { fireWebClient.setPath("abc1", "${basicExtSubmission.relPath}/folder/test.txt") } answers { nothing }
    }

    @Test
    fun `process submission with non existing file`() {
        val nfsFile = NfsFile("folder/test.txt", file, listOf(attribute))
        val section = ExtSection(type = "Study", files = listOf(left(nfsFile)))
        val submission = basicExtSubmission.copy(section = section)

        val processed = testInstance.persistSubmissionFiles(FilePersistenceRequest(submission))

        assertFireFile(processed, "folder/test.txt")
        verify(exactly = 1) { fireWebClient.save(file, testMd5, "", "") }
    }

    @Test
    fun `process submission with existing file`() {
        val nfsFile = NfsFile("folder/test.txt", file, listOf(attribute))
        val previousFile = FireFile("folder/test.txt", "abc1", testMd5, 1, listOf(attribute))
        val previousFiles = mapOf(Pair(testMd5, previousFile))
        val section = ExtSection(type = "Study", files = listOf(left(nfsFile)))
        val submission = basicExtSubmission.copy(section = section)
        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)

        val processed = testInstance.persistSubmissionFiles(request)

        assertFireFile(processed, "folder/test.txt")
        verify(exactly = 0) { fireWebClient.save(file, testMd5, "", "") }
    }

    @Test
    fun `process submission replacing files`() {
        val nfsFile = NfsFile("folder/test.txt", file, listOf(attribute))
        val previousFile = FireFile("folder/test.txt", "abc1", "a-different-md5", 1, listOf(attribute))
        val previousFiles = mapOf(Pair("a-different-md5", previousFile))
        val section = ExtSection(type = "Study", files = listOf(left(nfsFile)))
        val submission = basicExtSubmission.copy(section = section)
        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)

        val processed = testInstance.persistSubmissionFiles(request)

        assertFireFile(processed, "folder/test.txt")
        verify(exactly = 1) { fireWebClient.save(file, testMd5, "", "") }
    }

    @Test
    fun `process submission with path changed`() {
        val nfsFile = NfsFile("new-folder/test.txt", file, listOf(attribute))
        val previousFile = FireFile("old-folder/test.txt", "abc1", testMd5, 1, listOf(attribute))
        val previousFiles = mapOf(Pair(testMd5, previousFile))
        val section = ExtSection(type = "Study", files = listOf(left(nfsFile)))
        val submission = basicExtSubmission.copy(section = section)
        val request = FilePersistenceRequest(submission, previousFiles = previousFiles)

        val processed = testInstance.persistSubmissionFiles(request)

        assertFireFile(processed, "new-folder/test.txt")
        verify(exactly = 0) { fireWebClient.save(file, testMd5, "", "") }
    }

    private fun assertFireFile(processed: ExtSubmission, fileName: String) {
        assertThat(processed.section.files).hasSize(1)
        processed.section.files.first().ifLeft {
            it as FireFile
            assertThat(it.fireId).isEqualTo("abc1")
            assertThat(it.fileName).isEqualTo(fileName)
            assertThat(it.md5).isEqualTo(testMd5)
            assertThat(it.size).isEqualTo(1)
            assertThat(it.attributes).containsExactly(attribute)
        }
    }
}
