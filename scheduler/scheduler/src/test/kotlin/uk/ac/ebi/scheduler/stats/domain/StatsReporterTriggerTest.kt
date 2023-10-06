package uk.ac.ebi.scheduler.stats.domain

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.CoresSpec.FOUR_CORES
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec.Companion.EIGHT_GB
import arrow.core.Try
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.stats.api.Persistence
import uk.ac.ebi.scheduler.stats.api.StatsReporterProperties

@ExtendWith(MockKExtension::class)
class StatsReporterTriggerTest(
    @MockK private val appProperties: AppProperties,
    @MockK private val clusterOperations: ClusterOperations,
    @MockK private val notificationsSender: NotificationsSender,
) {
    private val testInstance = StatsReporterTrigger(appProperties, properties, clusterOperations, notificationsSender)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        setUpAppProperties()
    }

    @Test
    fun `trigger stats reporter job`(
        @MockK job: Job,
    ) {
        val jobSpecs = slot<JobSpec>()
        val jobReport = slot<Report>()

        every { job.id } returns "ABC123"
        every { job.queue } returns "submissions-releaser-queue"
        every { notificationsSender.send(capture(jobReport)) } answers { nothing }
        every { clusterOperations.triggerJob(capture(jobSpecs)) } returns Try.just(job)

        testInstance.triggerStatsReporter()

        verifyJobSpecs(jobSpecs.captured)
        verify(exactly = 1) {
            notificationsSender.send(jobReport.captured)
            clusterOperations.triggerJob(jobSpecs.captured)
        }
    }

    private fun setUpAppProperties() {
        every { appProperties.appsFolder } returns "apps-folder"
        every { appProperties.javaHome } returns "/home/jdk11"
        every { appProperties.ssh.user } returns "test-user"
        every { appProperties.ssh.sshKey } returns "test-ssh-key"
        every { appProperties.ssh.server } returns "test-server"
    }

    private fun verifyJobSpecs(specs: JobSpec) {
        assertThat(specs.ram).isEqualTo(EIGHT_GB)
        assertThat(specs.cores).isEqualTo(FOUR_CORES)
        assertThat(specs.command).isEqualTo(
            """
            "module load openjdk-11.0.1-gcc-9.3.0-unymjzh; \
            java \
            -Dsun.jnu.encoding=UTF-8 -Xmx6g \
            -jar apps-folder/stats-reporter-task-1.0.0.jar \
            --spring.data.mongodb.uri=mongodb://root:admin@localhost:27017/dev?authSource=admin\&replicaSet=biostd01 \
            --spring.data.mongodb.database=dev \
            --app.outputPath=/stats/output \
            --app.publishPath=/stats/publish \
            --app.ssh.user=test-user \
            --app.ssh.key=test-ssh-key \
            --app.ssh.server=test-server"
            """.trimIndent()
        )
    }

    companion object {
        val properties = StatsReporterProperties(
            outputPath = "/stats/output",
            publishPath = "/stats/publish",
            persistence = Persistence(
                database = "dev",
                uri = "mongodb://root:admin@localhost:27017/dev?authSource=admin\\&replicaSet=biostd01",
            ),
        )
    }
}
