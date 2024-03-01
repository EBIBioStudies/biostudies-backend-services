package ebi.ac.uk.paths

import java.nio.file.Path

const val FILES_PATH = "Files"

class SubmissionFolderResolver(
    private val includeSecretKey: Boolean,
    private val privateSubPath: Path,
    private val publicSubPath: Path,
) {
    fun getPublicSubFolder(submissionRelPath: String): Path {
        return publicSubPath.resolve(submissionRelPath)
    }

    fun getPrivateSubFolderRoot(secret: String): Path {
        return if (includeSecretKey) privateSubPath.resolve(secret.take(2)) else privateSubPath
    }

    fun getPrivateSubFolder(secretKey: String, relPath: String): Path {
        return when (includeSecretKey) {
            true -> getPrivateSubFolderRoot(secretKey).resolve("${secretKey.substring(2)}/$relPath")
            else -> privateSubPath.resolve(relPath)
        }
    }
}
