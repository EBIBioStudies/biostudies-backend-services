package uk.ac.ebi.biostd.client.cli.services

import ebi.ac.uk.api.security.UserProfile
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.config.DataMoverConfig
import uk.ac.ebi.biostd.client.cluster.api.SlurmClusterClient
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.io.File
import java.nio.file.Path
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ExtendWith(MockKExtension::class)
internal class DataMoverUserFilesServiceTest(
    @param:MockK private val profile: UserProfile,
    @param:MockK private val clusterClient: SlurmClusterClient,
) {
    private val testInstance = DataMoverUserFilesService(config)

    @BeforeEach
    fun beforeEach() {
        mockkObject(SlurmClusterClient)
        every { profile.secret } returns "ab/secret-a123"
        every { profile.uploadType } returns "nfs"
        every {
            SlurmClusterClient.create(
                config.sshKey.toString(),
                config.sshServer,
                config.logsPath,
                config.wrapperPath,
            )
        } returns clusterClient
    }

    @AfterEach
    fun afterEach() {
        clearAllMocks()
        unmockkObject(SlurmClusterClient)
    }

    @Test
    fun `submit synchronously and wait up to configured job duration`() =
        runTest {
            val spec = slot<JobSpec>()
            coEvery { clusterClient.triggerJobSync(capture(spec), 5.seconds, 1440.minutes) } returns job

            testInstance.upload(profile, source, "incoming")

            assertThat(spec.captured.queue).isEqualTo(DataMoverQueue)
            assertThat(spec.captured.minutes).isEqualTo(1440)
            assertThat(spec.captured.command).contains("/nfs/ab/secret-a123/incoming")
            verify(exactly = 1) {
                SlurmClusterClient.create("/key", "cluster", "/logs", "/wrapper")
            }
            coVerify(exactly = 1) { clusterClient.triggerJobSync(any(), 5.seconds, 1440.minutes) }
        }

    @Test
    fun `select FTP filesystem root from authenticated profile`() =
        runTest {
            every { profile.uploadType } returns "ftp"
            val spec = slot<JobSpec>()
            coEvery { clusterClient.triggerJobSync(capture(spec), any(), any()) } returns job

            testInstance.upload(profile, source, "")

            assertThat(spec.captured.command).contains("/ftp/ab/secret-a123")
        }

    @Test
    fun `propagate synchronous job failure`() =
        runTest {
            coEvery { clusterClient.triggerJobSync(any(), any(), any()) } throws IllegalStateException("Await condition expired")

            val error = assertThrows<IllegalStateException> { testInstance.upload(profile, source, "") }

            assertThat(error).hasMessage("Await condition expired")
        }

    @Test
    fun `reject missing storage root instead of guessing destination`() =
        runTest {
            every { profile.uploadType } returns "unsupported"

            assertThrows<IllegalStateException> { testInstance.upload(profile, source, "") }

            coVerify(exactly = 0) { clusterClient.triggerJobSync(any(), any(), any()) }
        }

    private companion object {
        val job = Job("12345", "datamover", "/logs/345/12345_OUT")
        val source = File("/source/data")
        val config =
            DataMoverConfig(
                sshServer = "cluster",
                sshKey = Path.of("/key"),
                wrapperPath = "/wrapper",
                logsPath = "/logs",
                userFilesRoots = mapOf("nfs" to Path.of("/nfs"), "ftp" to Path.of("/ftp")),
            )
    }
}
