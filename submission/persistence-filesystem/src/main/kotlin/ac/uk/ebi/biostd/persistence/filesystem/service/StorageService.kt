package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFtpService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS

// TODO delete
@Suppress("LongParameterList")
class StorageService(
    private val fireFtpService: FireFtpService,
    private val fireFilesService: FireFilesService,
    private val nfsFtpService: NfsFtpService,
    private val nfsFilesService: NfsFilesService,
) : FileStorageService {
    override fun cleanSubmissionFiles(submission: ExtSubmission) {
        TODO("Not yet implemented")
    }

    override fun persistSubmissionFiles(submission: ExtSubmission): ExtSubmission {
        TODO("Not yet implemented")
    }

    override fun releaseSubmissionFiles(submission: ExtSubmission) {
        TODO("Not yet implemented")
    }

    // TODO this class seems rather unnceseary
//    override fun persistSubmissionFile(request: FilePersistenceRequest): ExtFile =
//        TODO()
////        when (request.storageMode) {
////            FIRE -> fireFilesService.persistSubmissionFile(request)
////            NFS -> nfsFilesService.persistSubmissionFile(request)
////        }
//
//    override fun cleanSubmissionFile(file: ExtFile, storageMode: StorageMode) =
//        when (storageMode) {
//            FIRE -> fireFilesService.cleanSubmissionFile(file)
//            NFS -> nfsFilesService.cleanSubmissionFile(file)
//        }
//
//    override fun releaseSubmissionFile(file: ExtFile, storageMode: StorageMode) =
//        when (storageMode) {
//            FIRE -> fireFtpService.releaseSubmissionFile(file)
//            NFS -> nfsFtpService.releaseSubmissionFile(file)
//        }
}
