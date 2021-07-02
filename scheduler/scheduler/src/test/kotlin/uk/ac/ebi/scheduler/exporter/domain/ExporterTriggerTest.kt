package uk.ac.ebi.scheduler.exporter.domain

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec.Companion.TWENTYFOUR_GB
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
import uk.ac.ebi.scheduler.exporter.api.ExporterProperties
import uk.ac.ebi.scheduler.releaser.api.BioStudies
import uk.ac.ebi.scheduler.releaser.domain.RELEASER_CORES

@ExtendWith(MockKExtension::class)
class ExporterTriggerTest(
    @MockK private val job: Job,
    @MockK private val appProperties: AppProperties,
    @MockK private val clusterOperations: ClusterOperations,
    @MockK private val notificationsSender: NotificationsSender
) {
    private val jobSpecs = slot<JobSpec>()
    private val jobReport = slot<Report>()
    private val testInstance = ExporterTrigger(appProperties, testProperties(), clusterOperations, notificationsSender)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockJob()
        mockClusterOperations()
        mockApplicationProperties()
    }

    @Test
    fun triggerPublicExport() {
        testInstance.triggerPublicExport()
        verifyClusterOperations()
        verifyJobSpecs(jobSpecs.captured)
    }
    private fun mockApplicationProperties() = every { appProperties.appsFolder } returns "/apps-folder"

    private fun verifyClusterOperations() {
        verify(exactly = 1) { notificationsSender.send(jobReport.captured) }
        verify(exactly = 1) { clusterOperations.triggerJob(jobSpecs.captured) }
    }

    private fun verifyJobSpecs(specs: JobSpec) {
        assertThat(specs.ram).isEqualTo(TWENTYFOUR_GB)
        assertThat(specs.cores).isEqualTo(RELEASER_CORES)
        assertThat(specs.command).isEqualTo(
            """
            java -Dsun.jnu.encoding=UTF-8 -Xmx6g -jar /apps-folder/exporter-task-1.0.0.jar \
            --app.fileName=publicOnlyStudies \
            --app.outputPath=/an/output/path \
            --app.bioStudies.url=http://localhost:8080 \
            --app.bioStudies.user=admin_user@ebi.ac.uk \
            --app.bioStudies.password=123456
            """.trimIndent()
        )
    }

    private fun mockClusterOperations() {
        every { notificationsSender.send(capture(jobReport)) } answers { nothing }
        every { clusterOperations.triggerJob(capture(jobSpecs)) } returns Try.just(job)
    }

    private fun mockJob() {
        every { job.id } returns "ABC123"
        every { job.queue } returns "submissions-releaser-queue"
    }

    private fun testProperties() = ExporterProperties().apply {
        this.fileName = "publicOnlyStudies"
        this.outputPath = "/an/output/path"

        this.bioStudies = BioStudies().apply {
            url = "http://localhost:8080"
            user = "admin_user@ebi.ac.uk"
            password = "123456"
        }
    }
}
