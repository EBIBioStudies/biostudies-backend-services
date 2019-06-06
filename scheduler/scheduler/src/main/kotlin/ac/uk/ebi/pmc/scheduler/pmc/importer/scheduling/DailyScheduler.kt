package ac.uk.ebi.pmc.scheduler.pmc.importer.scheduling

import ac.uk.ebi.pmc.scheduler.pmc.importer.api.PmcLoaderService
import org.springframework.scheduling.annotation.Scheduled

class DailyScheduler(private val pmcLoader: PmcLoaderService) {

    @Scheduled(cron = "0 0 6 * * ?")
    fun dailyLoad() {
        pmcLoader.loadFile("/nfs/production3/ma/home/biostudy/EPMC-export/daily")
    }

    @Scheduled(cron = "0 0 6 * * ?")
    fun dailyProcess() {
        pmcLoader.triggerProcessor()
    }

    // @Scheduled(cron = "0 0 6 * * ?")
    @Scheduled(fixedRate = 20000)
    fun dailySubmission() {
        pmcLoader.triggerSubmitter()
    }
}
