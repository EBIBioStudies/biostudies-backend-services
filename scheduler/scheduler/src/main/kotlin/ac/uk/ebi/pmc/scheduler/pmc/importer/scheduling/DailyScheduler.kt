package ac.uk.ebi.pmc.scheduler.pmc.importer.scheduling

import ac.uk.ebi.pmc.scheduler.pmc.importer.api.PmcLoaderService
import org.springframework.scheduling.annotation.Scheduled

class DailyScheduler(private val pmcLoader: PmcLoaderService) {

    @Scheduled(fixedRate = 20000)
    fun dailyLoad() {
        pmcLoader.loadFile("/nfs/production3/ma/home/biostudy/EPMC-export/daily")
    }

}
