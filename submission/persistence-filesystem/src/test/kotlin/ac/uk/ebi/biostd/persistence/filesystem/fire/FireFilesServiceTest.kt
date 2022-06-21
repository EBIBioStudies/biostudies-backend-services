package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import arrow.core.Either
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.FileProcessingService

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class FireFilesServiceTest(
    @MockK private val fireService: FireService,
    @MockK private val fileProcessingService: FileProcessingService,
    tempFolder: TemporaryFolder,
) {
    private val submission = basicExtSubmission
    private val fireFile = FireFile("test.txt", "Files/test.txt", "abc1", "md5", 1, ExtFileType.FILE, emptyList())
    private val nfsFile = createNfsFile("folder", "Files/folder", tempFolder.createFile("dummy.txt"))
    private val testInstance = FireFilesService(fireService, fileProcessingService)

    @Test
    fun persistSubmissionFiles() {
        every { fireService.cleanFtp(submission) } answers { nothing }
        every { fireService.getOrPersist(submission, nfsFile) } answers { fireFile }
        every { fileProcessingService.processFiles(submission, any()) } answers {
            val function: (file: ExtFile, index: Int) -> ExtFile = secondArg()
            function(nfsFile, 1)
            submission.copy(section = submission.section.copy(files = listOf(Either.left(fireFile))))
        }

        val response = testInstance.persistSubmissionFiles(FilePersistenceRequest(submission))

        assertThat(response.section.files.first()).hasLeftValueSatisfying { assertThat(it).isEqualTo(fireFile) }
    }
}
