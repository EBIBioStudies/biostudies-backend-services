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

class NfsFtpService(
    private val folderResolver: SubmissionFolderResolver,
) : FtpService {
    override fun releaseSubmissionFile(file: ExtFile, subRelPath: String): ExtFile {
        val nfsFile = file as NfsFile
        val ftpFolder = getFtpFolder(subRelPath).toPath()
        val subFolder = folderResolver.getSubFolder(subRelPath)
        FileUtils.createHardLink(nfsFile.file, subFolder, ftpFolder, Permissions(RW_R__R__, RWXR_XR_X))
        return nfsFile
    }

    private fun getFtpFolder(relPath: String): File =
        FileUtils.getOrCreateFolder(folderResolver.getSubmissionFtpFolder(relPath), RWXR_XR_X).toFile()
}
