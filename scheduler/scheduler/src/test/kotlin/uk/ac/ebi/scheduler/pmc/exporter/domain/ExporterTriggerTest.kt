package uk.ac.ebi.scheduler.pmc.exporter.domain

import ac.uk.ebi.scheduler.properties.ExporterMode
import ac.uk.ebi.scheduler.properties.ExporterMode.PMC
import ac.uk.ebi.scheduler.properties.ExporterMode.PUBLIC_ONLY
import arrow.core.Try
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
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
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec.Companion.TWENTYFOUR_GB
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.pmc.exporter.api.BioStudies
import uk.ac.ebi.scheduler.pmc.exporter.api.ExporterProperties
import uk.ac.ebi.scheduler.pmc.exporter.api.Ftp
import uk.ac.ebi.scheduler.pmc.exporter.api.Persistence
import uk.ac.ebi.scheduler.pmc.exporter.api.Pmc
import uk.ac.ebi.scheduler.pmc.exporter.api.PublicOnly

@ExtendWith(MockKExtension::class)
class ExporterTriggerTest(
    @MockK private val job: Job,
    @MockK private val appProperties: AppProperties,
    @MockK private val clusterClient: ClusterClient,
    @MockK private val pcmNotificationsSender: NotificationsSender,
    @MockK private val schedulerNotificationsSender: NotificationsSender,
) {
    private val jobSpecs = slot<JobSpec>()
    private val jobReport = slot<Report>()
    private val testInstance =
        ExporterTrigger(
            appProperties,
            testProperties(),
            clusterClient,
            pcmNotificationsSender,
            schedulerNotificationsSender,
        )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { job.id } returns "ABC123"
        every { job.queue } returns "standard"
        every { job.logsPath } returns "/the/logs/path"

        every { appProperties.javaHome } returns "/home/jdk11"
        every { appProperties.appsFolder } returns "/apps-folder"

        coEvery { clusterClient.triggerJobAsync(capture(jobSpecs)) } returns Try.just(job)
        coEvery { pcmNotificationsSender.send(capture(jobReport)) } answers { nothing }
        coEvery { schedulerNotificationsSender.send(capture(jobReport)) } answers { nothing }
    }

    @Test
    fun triggerPmcExport() =
        runTest {
            testInstance.triggerPmcExport()
            coVerify(exactly = 1) {
                clusterClient.triggerJobAsync(jobSpecs.captured)
                pcmNotificationsSender.send(jobReport.captured)
            }
            verify { schedulerNotificationsSender wasNot called }
            verifyJobSpecs(jobSpecs.captured, PMC, "pmcFile", "/an/output/path/1")
        }

    @Test
    fun triggerPublicExport() =
        runTest {
            testInstance.triggerPublicExport()
            coVerify(exactly = 1) {
                clusterClient.triggerJobAsync(jobSpecs.captured)
                schedulerNotificationsSender.send(jobReport.captured)
            }
            verify { pcmNotificationsSender wasNot called }
            verifyJobSpecs(jobSpecs.captured, PUBLIC_ONLY, "publicOnlyStudies", "/an/output/path/2")
        }

    private fun verifyJobSpecs(
        specs: JobSpec,
        mode: ExporterMode,
        fileName: String,
        outputPath: String,
    ) {
        assertThat(specs.ram).isEqualTo(TWENTYFOUR_GB)
        assertThat(specs.cores).isEqualTo(FOUR_CORES)
        assertThat(specs.command).isEqualTo(
            """
            "module load openjdk-11.0.1-gcc-9.3.0-unymjzh; \
            java \
            -Dsun.jnu.encoding=UTF-8 -Xmx6g \
            -jar /apps-folder/exporter-task-1.0.0.jar \
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
            --app.bioStudies.password=123456"
            """.trimIndent(),
        )
    }

    private fun testProperties() =
        ExporterProperties().apply {
            this.tmpFilesPath = "/a/tmp/path"
            this.pmc =
                Pmc().apply {
                    fileName = "pmcFile"
                    outputPath = "/an/output/path/1"
                }

            this.publicOnly =
                PublicOnly().apply {
                    fileName = "publicOnlyStudies"
                    outputPath = "/an/output/path/2"
                }

            this.ftp =
                Ftp().apply {
                    host = "localhost"
                    user = "admin"
                    password = "123456"
                    port = 21
                }

            this.persistence =
                Persistence().apply {
                    database = "dev"
                    uri = "mongodb://root:admin@localhost:27017/dev?authSource=admin\\&replicaSet=biostd01"
                }

            this.bioStudies =
                BioStudies().apply {
                    url = "http://localhost:8080"
                    user = "admin_user@ebi.ac.uk"
                    password = "123456"
                }
        }
}
