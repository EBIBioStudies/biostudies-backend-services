package uk.ac.ebi.scheduler.releaser.domain

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec.Companion.EIGHT_GB
import ac.uk.ebi.scheduler.properties.ReleaserMode
import ac.uk.ebi.scheduler.properties.ReleaserMode.GENERATE_FTP_LINKS
import ac.uk.ebi.scheduler.properties.ReleaserMode.NOTIFY
import ac.uk.ebi.scheduler.properties.ReleaserMode.RELEASE
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
import uk.ac.ebi.scheduler.releaser.api.BioStudies
import uk.ac.ebi.scheduler.releaser.api.NotificationTimes
import uk.ac.ebi.scheduler.releaser.api.Rabbitmq
import uk.ac.ebi.scheduler.releaser.api.SubmissionReleaserProperties

@ExtendWith(MockKExtension::class)
class SubmissionReleaserTriggerTest(
    @MockK private val job: Job,
    @MockK private val appProperties: AppProperties,
    @MockK private val clusterOperations: ClusterOperations,
    @MockK private val notificationsSender: NotificationsSender
) {
    private val jobSpecs = slot<JobSpec>()
    private val jobReport = slot<Report>()
    private val properties = testProperties()
    private val trigger = SubmissionReleaserTrigger(appProperties, properties, clusterOperations, notificationsSender)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockJob()
        mockClusterOperations()
        mockApplicationProperties()
    }

    @Test
    fun triggerSubmissionReleaser() {
        trigger.triggerSubmissionReleaser()

        verifyClusterOperations()
        verifyJobSpecs(jobSpecs.captured, mode = RELEASE)
    }

    @Test
    fun triggerSubmissionReleaseNotifier() {
        trigger.triggerSubmissionReleaseNotifier()

        verifyClusterOperations()
        verifyJobSpecs(jobSpecs.captured, mode = NOTIFY)
    }

    @Test
    fun triggerFtpLinksGenerator() {
        trigger.triggerFtpLinksGenerator()

        verifyClusterOperations()
        verifyJobSpecs(jobSpecs.captured, mode = GENERATE_FTP_LINKS)
    }

    private fun verifyClusterOperations() {
        verify(exactly = 1) { notificationsSender.send(jobReport.captured) }
        verify(exactly = 1) { clusterOperations.triggerJob(jobSpecs.captured) }
    }

    private fun verifyJobSpecs(specs: JobSpec, mode: ReleaserMode) {
        assertThat(specs.ram).isEqualTo(EIGHT_GB)
        assertThat(specs.cores).isEqualTo(RELEASER_CORES)
        assertThat(specs.command).isEqualTo(
            """
            java -Dsun.jnu.encoding=UTF-8 -jar apps-folder/submission-releaser-task-1.0.0.jar \
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
            """.trimIndent()
        )
    }

    private fun mockApplicationProperties() = every { appProperties.appsFolder } returns "apps-folder"

    private fun mockClusterOperations() {
        every { notificationsSender.send(capture(jobReport)) } answers { nothing }
        every { clusterOperations.triggerJob(capture(jobSpecs)) } returns Try.just(job)
    }

    private fun mockJob() {
        every { job.id } returns "ABC123"
        every { job.queue } returns "submissions-releaser-queue"
    }

    private fun testProperties(): SubmissionReleaserProperties {
        val bioStudies = BioStudies().apply {
            url = "http://localhost:8080"
            user = "admin_user@ebi.ac.uk"
            password = "123456"
        }

        val rabbitmq = Rabbitmq().apply {
            host = "localhost"
            user = "manager"
            password = "manager-local"
            port = 5672
        }

        val notificationTimes = NotificationTimes().apply {
            firstWarningDays = 60
            secondWarningDays = 30
            thirdWarningDays = 7
        }

        return SubmissionReleaserProperties().apply {
            this.rabbitmq = rabbitmq
            this.bioStudies = bioStudies
            this.notificationTimes = notificationTimes
        }
    }
}
