package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionCleaner(
    private val systemService: FileSystemService,
    private val queryService: SubmissionPersistenceQueryService,
) {
    fun cleanCurrentVersion(accNo: String) {
        val sub = queryService.findExtByAccNo(accNo, includeFileListFiles = true)
        if (sub != null) {
            logger.info { "${sub.accNo} ${sub.owner} Started cleaning files of version ${sub.version}" }
            systemService.cleanFolder(sub)
            logger.info { "${sub.accNo} ${sub.owner} Finished cleaning files of version ${sub.version}" }
        }
    }
}
