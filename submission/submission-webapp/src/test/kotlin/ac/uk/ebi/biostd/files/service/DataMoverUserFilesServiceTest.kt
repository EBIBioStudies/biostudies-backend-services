package ac.uk.ebi.biostd.files.service

import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.nio.file.Path

@ExtendWith(MockKExtension::class)
internal class DataMoverUserFilesServiceTest(
    @param:MockK private val clusterClient: ClusterClient,
    @param:MockK private val user: SecurityUser,
) {
    private val testInstance = DataMoverUserFilesService(clusterClient)

    @BeforeEach
    fun beforeEach() {
        every { user.userFolder } returns NfsUserFolder(USER_FOLDER, USER_FOLDER)
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `trigger asynchronous datamover job into user folder`() =
        runTest {
            val spec = slot<JobSpec>()
            coEvery { clusterClient.triggerJobAsync(capture(spec)) } returns Result.success(JOB)

            val jobId = testInstance.upload(user, SOURCE.toString(), RELATIVE_PATH)

            assertThat(jobId).isEqualTo(JOB.id)
            assertThat(spec.captured.queue).isEqualTo(DataMoverQueue)
            assertThat(spec.captured.minutes).isEqualTo(1440)
            assertThat(spec.captured.command)
                .contains("source='$SOURCE'")
                .contains("target='$USER_FOLDER/$RELATIVE_PATH'")
                .contains("find -L \"\$source\" -type f")
                .contains("--partial-dir=.rsync-partial")
            coVerify(exactly = 1) { clusterClient.triggerJobAsync(any()) }
        }

    @Test
    fun `reject relative cluster source path`() =
        runTest {
            val error = assertThrows<IllegalArgumentException> { testInstance.upload(user, "relative/source", RELATIVE_PATH) }

            assertThat(error).hasMessage("The cluster source path must be absolute: relative/source")
            coVerify(exactly = 0) { clusterClient.triggerJobAsync(any()) }
        }

    @Test
    fun `reject destination outside user folder`() =
        runTest {
            val error = assertThrows<IllegalArgumentException> { testInstance.upload(user, SOURCE.toString(), "../outside") }

            assertThat(error).hasMessage("The user file path escapes the user folder: ../outside")
            coVerify(exactly = 0) { clusterClient.triggerJobAsync(any()) }
        }

    private companion object {
        val JOB = Job(id = "12345", queue = "datamover", logsPath = "/logs/345/12345_OUT")
        val SOURCE: Path = Path.of("/shared/source")
        val USER_FOLDER: Path = Path.of("/users/ab/secret-a123")
        const val RELATIVE_PATH = "incoming"
    }
}
