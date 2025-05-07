package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.paths.FolderType
import ebi.ac.uk.paths.FolderType.FTP
import ebi.ac.uk.paths.FolderType.NFS
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.nio.file.Path
import java.nio.file.Paths

internal class SubFolderResolver(
    private val properties: ApplicationProperties,
) : SubmissionFolderResolver {
    override fun getSubmisisonFolder(
        sub: ExtSubmission,
        folderType: FolderType,
    ): Path =
        when (sub.released) {
            true -> getPublicSubFolder(sub.relPath, folderType)
            else -> getPrivateSubFolder(sub.secretKey, sub.relPath, folderType)
        }

    override fun getPublicSubFolder(
        submissionRelPath: String,
        folderType: FolderType,
    ): Path =
        when (folderType) {
            FTP -> publicFtpPath.resolve(submissionRelPath)
            NFS -> publicSubPath.resolve(submissionRelPath)
        }

    override fun getPrivateSubFolder(
        secretKey: String,
        relPath: String,
        folderType: FolderType,
    ): Path {
        val baseFolder =
            when (folderType) {
                FTP -> privateFtpPath
                NFS -> privateSubPath
            }
        return when (includeSecretKey) {
            true -> baseFolder.resolve(secretKey.take(2)).resolve("${secretKey.substring(2)}/$relPath")
            else -> baseFolder.resolve(relPath)
        }
    }

    private val publicSubPath: Path get() = Paths.get(properties.persistence.publicSubmissionsPath)
    private val privateSubPath: Path get() = Paths.get(properties.persistence.privateSubmissionsPath)
    private val publicFtpPath: Path get() = Paths.get(properties.persistence.publicSubmissionFtpPath)
    private val privateFtpPath: Path get() = Paths.get(properties.persistence.privateSubmissionFtpPath)
    private val includeSecretKey: Boolean get() = properties.persistence.includeSecretKey
}
