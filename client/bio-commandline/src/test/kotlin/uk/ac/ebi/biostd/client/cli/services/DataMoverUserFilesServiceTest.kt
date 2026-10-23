package uk.ac.ebi.biostd.client.cli.services

import ebi.ac.uk.api.security.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.ac.ebi.biostd.client.cli.config.DataMoverConfig
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.io.File
import java.nio.file.Path
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class DataMoverUserFilesServiceTest {
    private val cluster = mockk<ClusterClient>()
    private val profile =
        mockk<UserProfile> {
            every { secret } returns "ab/secret-a123"
            every { uploadType } returns "nfs"
        }
    private val config =
        DataMoverConfig("cluster", Path.of("/key"), "/wrapper", "/logs", mapOf("nfs" to Path.of("/nfs"), "ftp" to Path.of("/ftp")))
    private val testInstance = DataMoverUserFilesService(config, cluster)
    private val job = Job("12345", "datamover", "/logs/345/12345_OUT")
    private val source = File("/source/data")

    @Test
    fun `submit synchronously and wait up to configured job duration`() =
        runTest {
            val spec = slot<JobSpec>()
            coEvery { cluster.triggerJobSync(capture(spec), 5.seconds, 1440.minutes) } returns job

            testInstance.upload(profile, source, "incoming")

            assertThat(spec.captured.queue).isEqualTo(DataMoverQueue)
            assertThat(spec.captured.minutes).isEqualTo(1440)
            assertThat(spec.captured.command).contains("/nfs/ab/secret-a123/incoming")
            coVerify(exactly = 1) { cluster.triggerJobSync(any(), 5.seconds, 1440.minutes) }
        }

    @Test
    fun `select FTP filesystem root from authenticated profile`() =
        runTest {
            every { profile.uploadType } returns "ftp"
            val spec = slot<JobSpec>()
            coEvery { cluster.triggerJobSync(capture(spec), any(), any()) } returns job

            testInstance.upload(profile, source, "")

            assertThat(spec.captured.command).contains("/ftp/ab/secret-a123")
        }

    @Test
    fun `propagate synchronous job failure`() =
        runTest {
            coEvery { cluster.triggerJobSync(any(), any(), any()) } throws IllegalStateException("Await condition expired")

            val error = assertThrows<IllegalStateException> { testInstance.upload(profile, source, "") }

            assertThat(error).hasMessage("Await condition expired")
        }

    @Test
    fun `reject missing storage root instead of guessing destination`() =
        runTest {
            every { profile.uploadType } returns "unsupported"

            assertThrows<IllegalStateException> { testInstance.upload(profile, source, "") }

            coVerify(exactly = 0) { cluster.triggerJobSync(any(), any(), any()) }
        }
}
