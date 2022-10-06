package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FileSystemServiceTest(
    @MockK private val fileStorageService: FileStorageService,
) {
    private val testInstance = FileSystemService(fileStorageService)

    @Test
    fun `clean folder`(
        @MockK submission: ExtSubmission
    ) {
        every { fileStorageService.cleanSubmissionFiles(submission) } answers { nothing }

        testInstance.cleanFolder(submission)

        verify(exactly = 1) { fileStorageService.cleanSubmissionFiles(submission) }
    }
}
