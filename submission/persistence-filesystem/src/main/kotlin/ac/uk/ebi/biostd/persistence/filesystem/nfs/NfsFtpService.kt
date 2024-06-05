package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ac.uk.ebi.biostd.persistence.filesystem.api.NfsReleaseMode
import ac.uk.ebi.biostd.persistence.filesystem.api.NfsReleaseMode.HARD_LINKS
import ac.uk.ebi.biostd.persistence.filesystem.api.NfsReleaseMode.MOVE
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmissionInfo
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWXR_X___
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.io.RW_R_____
import ebi.ac.uk.paths.SubmissionFolderResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

class NfsFtpService(
    private val releaseMode: NfsReleaseMode,
    private val folderResolver: SubmissionFolderResolver,
) : FtpService {
    override suspend fun releaseSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): ExtFile =
        withContext(Dispatchers.IO) {
            val nfsFile = file as NfsFile
            val publicSubFolder = getPublicFolder(sub.relPath)
            val privateSubFolder = folderResolver.getPrivateSubFolder(sub.secretKey, sub.relPath)

            when (releaseMode) {
                MOVE -> moveRelease(nfsFile, publicSubFolder)
                HARD_LINKS -> hardLinkRelease(nfsFile, privateSubFolder, publicSubFolder)
            }
        }

    private fun moveRelease(
        nfsFile: NfsFile,
        publicSubFolder: Path,
    ): NfsFile {
        val releasedFile = publicSubFolder.resolve(nfsFile.relPath).toFile()
        FileUtils.moveFile(nfsFile.file, releasedFile, Permissions(RW_R__R__, RWXR_XR_X))
        return nfsFile.copy(fullPath = releasedFile.absolutePath, file = releasedFile)
    }

    private fun hardLinkRelease(
        nfsFile: NfsFile,
        privateSubFolder: Path,
        publicSubFolder: Path,
    ): NfsFile {
        FileUtils.createHardLink(nfsFile.file, privateSubFolder, publicSubFolder, Permissions(RW_R__R__, RWXR_XR_X))
        return nfsFile
    }

    override suspend fun suppressSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): ExtFile =
        withContext(Dispatchers.IO) {
            val nfsFile = file as NfsFile
            val publicSubFolder = getPublicFolder(sub.relPath)
            val privateSubFolder = folderResolver.getPrivateSubFolder(sub.secretKey, sub.relPath)

            when (releaseMode) {
                MOVE -> moveSuppress(nfsFile, privateSubFolder)
                HARD_LINKS -> hardLinkSuppress(nfsFile, publicSubFolder)
            }
        }

    private fun moveSuppress(
        nfsFile: NfsFile,
        privateSubFolder: Path,
    ): NfsFile {
        val suppressedFile = privateSubFolder.resolve(nfsFile.relPath).toFile()
        FileUtils.moveFile(nfsFile.file, suppressedFile, Permissions(RW_R_____, RWXR_X___))
        return nfsFile.copy(fullPath = suppressedFile.absolutePath, file = suppressedFile)
    }

    private fun hardLinkSuppress(
        nfsFile: NfsFile,
        publicSubFolder: Path,
    ): NfsFile {
        val releasedFile = publicSubFolder.resolve(nfsFile.relPath).toFile()
        FileUtils.deleteFile(releasedFile)

        return nfsFile
    }

    private fun getPublicFolder(relPath: String): Path {
        return FileUtils.getOrCreateFolder(folderResolver.getPublicSubFolder(relPath), RWXR_XR_X)
    }
}
