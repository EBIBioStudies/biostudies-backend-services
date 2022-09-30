package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.forEachFile

private val logger = KotlinLogging.logger {}

class SubmissionRequestCleaner(
//    private val systemService: FileSystemService,
    private val nfsFilesService: NfsFilesService,
    private val fireFilesService: FireFilesService,
    private val serializationService: ExtSerializationService,
    private val queryService: SubmissionPersistenceQueryService,
    private val persistenceService: SubmissionPersistenceService,
) {
    fun cleanCurrentVersion(accNo: String, version: Int) {
        val request = queryService.getLoadedRequest(accNo, version)
        val sub = queryService.findExtByAccNo(accNo, includeFileListFiles = true)

        if (sub != null) {
            logger.info { "${sub.accNo} ${sub.owner} Started cleaning files of version ${sub.version}" }
            cleanSubmissionFiles(sub)
            logger.info { "${sub.accNo} ${sub.owner} Finished cleaning files of version ${sub.version}" }
        }

        persistenceService.saveSubmissionRequest(request.copy(status = CLEANED))
    }

    fun cleanSubmissionFiles(sub: ExtSubmission) = when (sub.storageMode) {
        NFS -> cleanNfsFiles(sub)
        FIRE -> cleanFireFiles(sub)
    }

    private fun cleanFireFiles(sub: ExtSubmission) {
        logger.info { "${sub.accNo} ${sub.owner} Started cleaning Current submission folder" }

        serializationService.forEachFile(sub) { file, index ->
            if (file is FireFile) {
                logger.info { "${sub.accNo} ${sub.owner} Stated Cleaning file $index, path='${file.filePath}'" }

                fireFilesService.cleanSubmissionFile(file)
                persistenceService.updateRequestIndex(sub.accNo, sub.version, index)

                logger.info { "${sub.accNo} ${sub.owner} Finished Cleaning file $index, path='${file.filePath}'" }
            }
        }

        logger.info { "${sub.accNo} ${sub.owner} Finished cleaning Current submission folder" }
    }

    private fun cleanNfsFiles(sub: ExtSubmission) {
        nfsFilesService.cleanSubmissionFiles(sub)
    }


}
