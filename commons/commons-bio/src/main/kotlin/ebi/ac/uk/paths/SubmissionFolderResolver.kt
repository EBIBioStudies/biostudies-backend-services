package ebi.ac.uk.paths

import ebi.ac.uk.extended.model.ExtSubmissionInfo
import java.nio.file.Path

const val FILES_PATH = "Files"

interface SubmissionFolderResolver {
    fun getSubmisisonFolder(
        sub: ExtSubmissionInfo,
        folderType: FolderType,
    ): Path

    fun getPublicSubFolder(
        submissionRelPath: String,
        folderType: FolderType = FolderType.NFS,
    ): Path

    fun getPrivateSubFolder(
        secretKey: String,
        relPath: String,
        folderType: FolderType = FolderType.NFS,
    ): Path

    fun getCopyPageTabPath(sub: ExtSubmissionInfo): Path
}

enum class FolderType {
    FTP,
    NFS,
}
