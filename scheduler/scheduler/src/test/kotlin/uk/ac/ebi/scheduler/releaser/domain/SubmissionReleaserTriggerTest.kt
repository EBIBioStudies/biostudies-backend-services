package uk.ac.ebi.scheduler.releaser.domain

import ac.uk.ebi.scheduler.properties.ReleaserMode
import ac.uk.ebi.scheduler.properties.ReleaserMode.GENERATE_FTP_LINKS
import ac.uk.ebi.scheduler.properties.ReleaserMode.NOTIFY
import ac.uk.ebi.scheduler.properties.ReleaserMode.RELEASE
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
import uk.ac.ebi.scheduler.releaser.api.BioStudies
import uk.ac.ebi.scheduler.releaser.api.NotificationTimes
import uk.ac.ebi.scheduler.releaser.api.Persistence
import uk.ac.ebi.scheduler.releaser.api.Rabbitmq
import uk.ac.ebi.scheduler.releaser.api.SubmissionReleaserProperties
import kotlin.Result.Companion.success

@ExtendWith(MockKExtension::class)
class SubmissionReleaserTriggerTest(
    @MockK private val job: Job,
    @MockK private val appProperties: AppProperties,
    @MockK private val clusterClient: ClusterClient,
    @MockK private val notificationsSender: NotificationsSender,
) {
    private val jobSpecs = slot<JobSpec>()
    private val jobReport = slot<Report>()
    private val properties = testProperties()
    private val trigger = SubmissionReleaserTrigger(appProperties, properties, clusterClient, notificationsSender)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        every { job.id } returns "ABC123"
        every { job.queue } returns "standard"

        every { appProperties.appsFolder } returns "apps-folder"
        every { appProperties.javaHome } returns "/home/jdk11"

        coEvery { clusterClient.triggerJobAsync(capture(jobSpecs)) } returns success(job)

        coEvery { notificationsSender.send(capture(jobReport)) } answers { nothing }
    }

    @Test
    fun triggerSubmissionReleaser() =
        runTest {
            trigger.triggerSubmissionReleaser()

            verifyClusterOperations()
            verifyJobSpecs(jobSpecs.captured, mode = RELEASE)
        }

    @Test
    fun triggerSubmissionReleaseNotifier() =
        runTest {
            trigger.triggerSubmissionReleaseNotifier()

            verifyClusterOperations()
            verifyJobSpecs(jobSpecs.captured, mode = NOTIFY)
        }

    @Test
    fun triggerFtpLinksGenerator() =
        runTest {
            trigger.triggerFtpLinksGenerator()

            verifyClusterOperations()
            verifyJobSpecs(jobSpecs.captured, mode = GENERATE_FTP_LINKS)
        }

    private fun verifyClusterOperations() {
        coVerify(exactly = 1) {
            notificationsSender.send(jobReport.captured)
            clusterClient.triggerJobAsync(jobSpecs.captured)
        }
    }

    private fun verifyJobSpecs(
        specs: JobSpec,
        mode: ReleaserMode,
    ) {
        assertThat(specs.ram).isEqualTo(EIGHT_GB)
        assertThat(specs.cores).isEqualTo(FOUR_CORES)
        assertThat(specs.command).isEqualTo(
            """
            module load openjdk-17.0.5_8-gcc-11.2.0-gsv4jnu; \
            java \
            -Dsun.jnu.encoding=UTF-8 -Xmx6g \
            -jar apps-folder/submission-releaser-task-1.0.0.jar \
            --spring.data.mongodb.uri=mongodb://root:admin@localhost:27017/dev?authSource=admin\&replicaSet=biostd01 \
            --spring.data.mongodb.database=dev \
            --spring.rabbitmq.host=localhost \
            --spring.rabbitmq.username=manager \
            --spring.rabbitmq.password=manager-local \
            --spring.rabbitmq.port=5672 \
            --app.mode=$mode \
            --app.bioStudies.url=http://localhost:8080 \
            --app.bioStudies.user=admin_user@ebi.ac.uk \
            --app.bioStudies.password=123456 \
            --app.notification-times.first-warning-days=60 \
            --app.notification-times.second-warning-days=30 \
            --app.notification-times.third-warning-days=7
            """.trimIndent(),
        )
    }

    private fun testProperties(): SubmissionReleaserProperties {
        val bioStudies =
            BioStudies().apply {
                url = "http://localhost:8080"
                user = "admin_user@ebi.ac.uk"
                password = "123456"
            }

        val persistence =
            Persistence().apply {
                database = "dev"
                uri = "mongodb://root:admin@localhost:27017/dev?authSource=admin\\&replicaSet=biostd01"
            }

        val rabbitmq =
            Rabbitmq().apply {
                host = "localhost"
                user = "manager"
                password = "manager-local"
                port = 5672
            }

        val notificationTimes =
            NotificationTimes().apply {
                firstWarningDays = 60
                secondWarningDays = 30
                thirdWarningDays = 7
            }

        return SubmissionReleaserProperties().apply {
            this.rabbitmq = rabbitmq
            this.bioStudies = bioStudies
            this.persistence = persistence
            this.notificationTimes = notificationTimes
        }
    }
}
