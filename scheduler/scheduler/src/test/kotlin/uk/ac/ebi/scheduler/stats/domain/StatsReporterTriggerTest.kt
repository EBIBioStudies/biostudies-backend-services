package uk.ac.ebi.scheduler.stats.domain

import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
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
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.CoresSpec.FOUR_CORES
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec.Companion.EIGHT_GB
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.stats.api.Persistence
import uk.ac.ebi.scheduler.stats.api.StatsReporterProperties

@ExtendWith(MockKExtension::class)
class StatsReporterTriggerTest(
    @MockK private val appProperties: AppProperties,
    @MockK private val clusterClient: ClusterClient,
    @MockK private val notificationsSender: NotificationsSender,
) {
    private val testInstance = StatsReporterTrigger(appProperties, properties, clusterClient, notificationsSender)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        setUpAppProperties()
    }

    @Test
    fun `trigger stats reporter job`(
        @MockK job: Job,
    ) = runTest {
        val jobSpecs = slot<JobSpec>()
        val jobReport = slot<Report>()

        every { job.id } returns "ABC123"
        every { job.queue } returns "standard"

        coEvery { notificationsSender.send(capture(jobReport)) } answers { nothing }
        coEvery { clusterClient.triggerJobAsync(capture(jobSpecs)) } returns Result.success(job)

        testInstance.triggerStatsReporter()

        verifyJobSpecs(jobSpecs.captured)
        coVerify(exactly = 1) {
            notificationsSender.send(jobReport.captured)
            clusterClient.triggerJobAsync(jobSpecs.captured)
        }
    }

    private fun setUpAppProperties() {
        every { appProperties.appsFolder } returns "apps-folder"
        every { appProperties.javaHome } returns "/home/jdk11"
    }

    private fun verifyJobSpecs(specs: JobSpec) {
        assertThat(specs.ram).isEqualTo(EIGHT_GB)
        assertThat(specs.cores).isEqualTo(FOUR_CORES)
        assertThat(specs.command).isEqualTo(
            """
            module load openjdk-17.0.5_8-gcc-11.2.0-gsv4jnu; \
            java \
            -Dsun.jnu.encoding=UTF-8 -Xmx6g \
            -jar apps-folder/stats-reporter-task-1.0.0.jar \
            --spring.data.mongodb.uri=mongodb://root:admin@localhost:27017/dev?authSource=admin\&replicaSet=biostd01 \
            --spring.data.mongodb.database=dev \
            --app.publishPath=/stats/publish
            """.trimIndent(),
        )
    }

    companion object {
        val properties =
            StatsReporterProperties(
                publishPath = "/stats/publish",
                persistence =
                    Persistence(
                        database = "dev",
                        uri = "mongodb://root:admin@localhost:27017/dev?authSource=admin\\&replicaSet=biostd01",
                    ),
            )
    }
}
