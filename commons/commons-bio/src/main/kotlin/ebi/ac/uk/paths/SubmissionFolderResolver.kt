package ebi.ac.uk.paths

import ebi.ac.uk.extended.model.ExtSubmission
import java.nio.file.Path

const val FILES_PATH = "Files"

interface SubmissionFolderResolver {
    fun getSubmisisonFolder(
        sub: ExtSubmission,
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
}

enum class FolderType {
    FTP,
    NFS,
}
