package ac.uk.ebi.biostd.submission.domain.cleanup

import ac.uk.ebi.biostd.common.properties.Mode.CLEAN_UP_USER_SPACE
import ac.uk.ebi.biostd.common.properties.Mode.NOTIFY_USER_SPACE_CLEAN_UP
import ac.uk.ebi.biostd.submission.domain.cleanup.ExtUserSpaceCleanUpService.CleanUpMode.CLEAN_UP
import ac.uk.ebi.biostd.submission.domain.cleanup.ExtUserSpaceCleanUpService.CleanUpMode.NOTIFY
import ac.uk.ebi.biostd.submission.domain.submitter.RemoteSubmitterExecutor
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cluster.model.Job

@ExtendWith(MockKExtension::class)
class ExtUserSpaceCleanUpServiceTest(
    @param:MockK private val remoteSubmitterExecutor: RemoteSubmitterExecutor,
    @param:MockK private val localUserSpaceCleanUpService: LocalUserSpaceCleanUpService,
) {
    private val testInstance =
        ExtUserSpaceCleanUpService(
            remoteSubmitterExecutor,
            localUserSpaceCleanUpService,
        )

    @BeforeEach
    fun beforeEach() = clearAllMocks()

    @Test
    fun `notify executes remotely when remote is enabled`() =
        runTest {
            coEvery { remoteSubmitterExecutor.executeRemotely(emptyList(), NOTIFY_USER_SPACE_CLEAN_UP) } returns remoteJob()

            testInstance.cleanUp(NOTIFY, remote = true)

            coVerify(exactly = 1) {
                remoteSubmitterExecutor.executeRemotely(emptyList(), NOTIFY_USER_SPACE_CLEAN_UP)
            }
            coVerify(exactly = 0) {
                localUserSpaceCleanUpService.sendNotifications()
            }
        }

    @Test
    fun `notify executes locally when remote is disabled`() =
        runTest {
            coEvery { localUserSpaceCleanUpService.sendNotifications() } returns Unit

            testInstance.cleanUp(NOTIFY, remote = false)

            coVerify(exactly = 1) {
                localUserSpaceCleanUpService.sendNotifications()
            }
            coVerify(exactly = 0) {
                remoteSubmitterExecutor.executeRemotely(any(), any())
            }
        }

    @Test
    fun `clean up executes remotely when remote is enabled`() =
        runTest {
            coEvery { remoteSubmitterExecutor.executeRemotely(emptyList(), CLEAN_UP_USER_SPACE) } returns remoteJob()

            testInstance.cleanUp(CLEAN_UP, remote = true)

            coVerify(exactly = 1) {
                remoteSubmitterExecutor.executeRemotely(emptyList(), CLEAN_UP_USER_SPACE)
            }
            coVerify(exactly = 0) {
                localUserSpaceCleanUpService.cleanUpUserSpaces()
            }
        }

    @Test
    fun `clean up executes locally when remote is disabled`() =
        runTest {
            coEvery { localUserSpaceCleanUpService.cleanUpUserSpaces() } returns Unit

            testInstance.cleanUp(CLEAN_UP, remote = false)

            coVerify(exactly = 1) {
                localUserSpaceCleanUpService.cleanUpUserSpaces()
            }
            coVerify(exactly = 0) {
                remoteSubmitterExecutor.executeRemotely(any(), any())
            }
        }

    private fun remoteJob() = Job("12345", "datamover", "/logs/12345")
}
