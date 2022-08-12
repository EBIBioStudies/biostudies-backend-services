package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class FileSystemService(
    private val filesService: FilesService,
    private val pageTabService: PageTabService,
) {
    fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission {
        logger.info { "${sub.accNo} ${sub.owner} Processing files of submission ${sub.accNo}" }

        val processedSubmission = filesService.persistSubmissionFiles(sub)
        val finalSub = pageTabService.generatePageTab(processedSubmission)

        logger.info { "${sub.accNo} ${sub.owner} Finished processing files of submission ${sub.accNo}" }

        return finalSub
    }

    fun cleanFolder(previousSubmission: ExtSubmission) {
        filesService.cleanSubmissionFiles(previousSubmission)
    }
}
