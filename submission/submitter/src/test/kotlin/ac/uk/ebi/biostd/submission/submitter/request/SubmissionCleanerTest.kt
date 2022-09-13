package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SubmissionCleanerTest(
    @MockK private val systemService: FileSystemService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
) {
    private val testInstance = SubmissionCleaner(systemService, queryService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `clean current version`() {
        every { systemService.cleanFolder(basicExtSubmission) } answers { nothing }
        every { queryService.findExtByAccNo("S-BSST0", includeFileListFiles = true) } returns basicExtSubmission

        testInstance.cleanCurrentVersion("S-BSST0")

        verify(exactly = 1) { systemService.cleanFolder(basicExtSubmission) }
    }

    @Test
    fun `clean current version when no previous version is found`() {
        every { queryService.findExtByAccNo("S-BSST0", includeFileListFiles = true) } returns null

        testInstance.cleanCurrentVersion("S-BSST0")

        verify(exactly = 0) { systemService.cleanFolder(any()) }
    }
}
