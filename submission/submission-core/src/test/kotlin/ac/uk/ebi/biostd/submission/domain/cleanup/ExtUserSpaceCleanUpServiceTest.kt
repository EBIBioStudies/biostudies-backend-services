package ac.uk.ebi.biostd.submission.domain.cleanup

import ac.uk.ebi.biostd.common.properties.Mode.CLEAN_UP_USER_SPACE
import ac.uk.ebi.biostd.persistence.common.service.CleanUpLogDataService
import ac.uk.ebi.biostd.submission.domain.cleanup.ExtUserSpaceCleanUpService.CleanUpMode.CLEAN_UP
import ac.uk.ebi.biostd.submission.domain.cleanup.LocalUserSpaceCleanUpService.CleanUpUser
import ac.uk.ebi.biostd.submission.domain.submitter.ExecutionArg
import ac.uk.ebi.biostd.submission.domain.submitter.RemoteSubmitterExecutor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cluster.model.Job
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class ExtUserSpaceCleanUpServiceTest(
    @param:MockK private val remoteSubmitterExecutor: RemoteSubmitterExecutor,
    @param:MockK private val localUserSpaceCleanUpService: LocalUserSpaceCleanUpService,
    @param:MockK private val cleanUpLogDataService: CleanUpLogDataService,
) {
    private val testInstance =
        ExtUserSpaceCleanUpService(
            remoteSubmitterExecutor,
            localUserSpaceCleanUpService,
            cleanUpLogDataService,
        )

    @Test
    fun `clean up dispatches one remote job per user and logs dispatch`() =
        runTest {
            val lastActivity = LocalDateTime.parse("2026-02-16T10:00:00")
            val cleanUpUser =
                CleanUpUser(
                    email = "cleanup@ebi.ac.uk",
                    lastActivity = lastActivity,
                    userSpacePath = "/data/users/cleanup@ebi.ac.uk",
                )
            val job = Job("12345", "datamover", "/logs/12345")

            coEvery { localUserSpaceCleanUpService.cleanUpUserSpaces() } returns listOf(cleanUpUser)
            coEvery {
                remoteSubmitterExecutor.executeRemotely(listOf(ExecutionArg("email", cleanUpUser.email)), CLEAN_UP_USER_SPACE)
            } returns job
            coEvery { cleanUpLogDataService.logCleanUp(any(), any(), any(), any()) } returns Unit

            testInstance.cleanUp(CLEAN_UP, remote = true)

            coVerify(exactly = 1) {
                remoteSubmitterExecutor.executeRemotely(listOf(ExecutionArg("email", cleanUpUser.email)), CLEAN_UP_USER_SPACE)
                cleanUpLogDataService.logCleanUp(
                    cleanUpUser.email,
                    job.id,
                    lastActivity,
                    cleanUpUser.userSpacePath,
                )
            }
            coVerify(exactly = 0) {
                cleanUpLogDataService.logCleanUpError(any(), any(), any(), any())
            }
        }
}
