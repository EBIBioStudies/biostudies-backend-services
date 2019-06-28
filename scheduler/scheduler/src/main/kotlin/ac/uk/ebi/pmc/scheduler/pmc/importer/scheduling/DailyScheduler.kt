package ac.uk.ebi.pmc.scheduler.pmc.importer.scheduling

import ac.uk.ebi.cluster.client.lsf.LOGS_PATH
import ac.uk.ebi.pmc.scheduler.pmc.importer.api.PmcLoaderService
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.ReportNotification
import org.springframework.scheduling.annotation.Scheduled

private const val SYSTEM_NAME = "Scheduler"

internal class DailyScheduler(
    private val pmcLoader: PmcLoaderService,
    private val notificationsSender: NotificationsSender
) {

    @Scheduled(cron = "0 0 6 * *")
    fun dailyLoad() {
        val file = "/nfs/production3/ma/home/biostudy/EPMC-export/daily"
        val job = pmcLoader.loadFile(file)
        notificationsSender.sent(ReportNotification(
            system = SYSTEM_NAME,
            subSystem = "PMC Loading Trigger",
            message = """
                |Trigger daily PMC loaded $file, cluster job: $job, 
                |logs will be available at $LOGS_PATH${job.id}_OUT""".trimMargin()))
    }

    @Scheduled(cron = "0 0 6 * *")
    fun dailyProcess() {
        val job = pmcLoader.triggerProcessor()
        notificationsSender.sent(ReportNotification(
            system = SYSTEM_NAME,
            subSystem = "PMC Processor Trigger",
            message = """
                |Trigger daily PMC processor, cluster job: $job, 
                |logs will be available at $LOGS_PATH${job.id}_OUT""".trimMargin()))
    }

    @Scheduled(cron = "0 0 6 * *")
    fun dailySubmission() {
        val job = pmcLoader.triggerSubmitter()
        notificationsSender.sent(ReportNotification(
            system = SYSTEM_NAME,
            subSystem = "PMC Submitter Trigger",
            message = """
                |Executed daily PMC submitter, cluster job: $job, 
                |logs will be available at $LOGS_PATH${job.id}_OUT""".trimIndent()))
    }
}
