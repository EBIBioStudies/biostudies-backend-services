package uk.ac.ebi.scheduler.exporter.domain

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec.Companion.TWENTYFOUR_GB
import ac.uk.ebi.scheduler.properties.ExporterMode
import ac.uk.ebi.scheduler.properties.ExporterMode.PMC
import ac.uk.ebi.scheduler.properties.ExporterMode.PUBLIC_ONLY
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
import uk.ac.ebi.scheduler.exporter.api.BioStudies
import uk.ac.ebi.scheduler.exporter.api.ExporterProperties
import uk.ac.ebi.scheduler.exporter.api.Ftp
import uk.ac.ebi.scheduler.exporter.api.Persistence
import uk.ac.ebi.scheduler.exporter.api.Pmc
import uk.ac.ebi.scheduler.exporter.api.PublicOnly
import uk.ac.ebi.scheduler.releaser.domain.RELEASER_CORES

@ExtendWith(MockKExtension::class)
class ExporterTriggerTest(
    @MockK private val job: Job,
    @MockK private val appProperties: AppProperties,
    @MockK private val clusterOperations: ClusterOperations,
    @MockK private val notificationsSender: NotificationsSender,
) {
    private val jobSpecs = slot<JobSpec>()
    private val jobReport = slot<Report>()
    private val testInstance = ExporterTrigger(appProperties, testProperties(), clusterOperations, notificationsSender)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { job.id } returns "ABC123"
        every { job.queue } returns "submissions-releaser-queue"
        every { notificationsSender.send(capture(jobReport)) } answers { nothing }
        every { clusterOperations.triggerJob(capture(jobSpecs)) } returns Try.just(job)
        every { appProperties.appsFolder } returns "/apps-folder"
        every { appProperties.javaHome } returns "/home/java"
    }

    @Test
    fun triggerPmcExport() {
        testInstance.triggerPmcExport()
        verifyClusterOperations()
        verifyJobSpecs(jobSpecs.captured, PMC, "pmcFile", "/an/output/path/1")
    }

    @Test
    fun triggerPublicExport() {
        testInstance.triggerPublicExport()
        verifyClusterOperations()
        verifyJobSpecs(jobSpecs.captured, PUBLIC_ONLY, "publicOnlyStudies", "/an/output/path/2")
    }

    private fun verifyClusterOperations() {
        verify(exactly = 1) { notificationsSender.send(jobReport.captured) }
        verify(exactly = 1) { clusterOperations.triggerJob(jobSpecs.captured) }
    }

    private fun verifyJobSpecs(specs: JobSpec, mode: ExporterMode, fileName: String, outputPath: String) {
        assertThat(specs.ram).isEqualTo(TWENTYFOUR_GB)
        assertThat(specs.cores).isEqualTo(RELEASER_CORES)
        assertThat(specs.command).isEqualTo(
            """
            /home/java/bin/java -Dsun.jnu.encoding=UTF-8 -Xmx6g -jar /apps-folder/exporter-task-1.0.0.jar \
            --app.mode=$mode \
            --app.fileName=$fileName \
            --app.outputPath=$outputPath \
            --app.tmpFilesPath=/a/tmp/path \
            --app.ftp.host=localhost \
            --app.ftp.user=admin \
            --app.ftp.password=123456 \
            --app.ftp.port=21 \
            --spring.data.mongodb.database=dev \
            --spring.data.mongodb.uri=mongodb://root:admin@localhost:27017/dev?authSource=admin\&replicaSet=biostd01 \
            --app.bioStudies.url=http://localhost:8080 \
            --app.bioStudies.user=admin_user@ebi.ac.uk \
            --app.bioStudies.password=123456
            """.trimIndent()
        )
    }

    private fun testProperties() = ExporterProperties().apply {
        this.tmpFilesPath = "/a/tmp/path"
        this.pmc = Pmc().apply {
            fileName = "pmcFile"
            outputPath = "/an/output/path/1"
        }

        this.publicOnly = PublicOnly().apply {
            fileName = "publicOnlyStudies"
            outputPath = "/an/output/path/2"
        }

        this.ftp = Ftp().apply {
            host = "localhost"
            user = "admin"
            password = "123456"
            port = 21
        }

        this.persistence = Persistence().apply {
            database = "dev"
            uri = "mongodb://root:admin@localhost:27017/dev?authSource=admin\\&replicaSet=biostd01"
        }

        this.bioStudies = BioStudies().apply {
            url = "http://localhost:8080"
            user = "admin_user@ebi.ac.uk"
            password = "123456"
        }
    }
}
