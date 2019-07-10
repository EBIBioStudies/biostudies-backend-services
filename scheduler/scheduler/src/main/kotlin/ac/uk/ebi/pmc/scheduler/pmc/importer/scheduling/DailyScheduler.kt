package ac.uk.ebi.pmc.scheduler.pmc.importer.scheduling

import ac.uk.ebi.pmc.scheduler.pmc.importer.domain.PmcLoaderService
import org.springframework.scheduling.annotation.Scheduled

internal class DailyScheduler(
    private val pmcLoaderService: PmcLoaderService
) {

    @Scheduled(cron = "0 0 6 * * *")
    fun dailyLoad() {
        pmcLoaderService.loadFile("/nfs/production3/ma/home/biostudy/EPMC-export/daily")
    }

    @Scheduled(cron = "0 0 7 * * *")
    fun dailyProcess() {
        pmcLoaderService.triggerProcessor()
    }

    @Scheduled(cron = "0 0 8 * * *")
    fun dailySubmission() {
        pmcLoaderService.triggerSubmitter()
    }
}
