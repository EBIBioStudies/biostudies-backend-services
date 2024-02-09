package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.io.File

// TODO these classes should be renamed to NfsReleaserService and FireReleaserService to decouple them from the FTP concept
class NfsFtpService(
    private val folderResolver: SubmissionFolderResolver,
) : FtpService {
    override suspend fun releaseSubmissionFile(file: ExtFile, subRelPath: String): ExtFile {
        return synchronized(this) {
            val nfsFile = file as NfsFile
            val subFolder = folderResolver.getSubFolder(subRelPath)
            val releasedFile = subFolder.resolve(nfsFile.relPath).toFile()

            FileUtils.moveFile(nfsFile.file, releasedFile, Permissions(RW_R__R__, RWXR_XR_X))
            nfsFile.copy(fullPath = releasedFile.absolutePath, file = releasedFile)
        }
    }
}
