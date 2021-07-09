package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService.processFiles
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient

private val logger = KotlinLogging.logger {}

class FireFilesService(
    private val fireWebClient: FireWebClient
) : FilesService {
    override fun persistSubmissionFiles(request: FilePersistenceRequest): ExtSubmission {
        val (submission, _, previousFiles) = request
        logger.info { "Starting processing files of submission ${submission.accNo} over FIRE" }

        val config = FireFileProcessingConfig(submission.relPath, fireWebClient, previousFiles)

        fun processFile(file: ExtFile): ExtFile =
            if (file is NfsFile) config.processNfsFile(file) else config.processFireFile(file as FireFile)

        val processed = processFiles(submission, ::processFile)

        logger.info { "Finishing processing files of submission ${submission.accNo} over FIRE" }

        return processed
    }
}
