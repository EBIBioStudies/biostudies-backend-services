package ac.uk.ebi.biostd.submission.domain.submission

import uk.ac.ebi.biostd.common.properties.ApplicationProperties
import uk.ac.ebi.biostd.extended.model.ExtSubmissionInfo
import uk.ac.ebi.biostd.paths.FolderType
import uk.ac.ebi.biostd.paths.FolderType.FTP
import uk.ac.ebi.biostd.paths.FolderType.NFS
import uk.ac.ebi.biostd.paths.SubmissionFolderResolver
import java.nio.file.Path
import java.nio.file.Paths

internal class SubFolderResolver(
    private val properties: ApplicationProperties,
) : SubmissionFolderResolver {
    override fun getSubmissionFolder(
        sub: ExtSubmissionInfo,
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

    override fun getFallbackPageTabPath(sub: ExtSubmissionInfo): Path {
        val basePath = Paths.get(properties.persistence.pageTabFallbackPath)
        return basePath.resolve(sub.relPath)
    }

    private val publicSubPath: Path get() = Paths.get(properties.persistence.publicSubmissionsPath)
    private val privateSubPath: Path get() = Paths.get(properties.persistence.privateSubmissionsPath)
    private val privateFtpPath: Path get() = Paths.get(properties.persistence.privateSubmissionFtpOutPath)
    private val publicFtpPath: Path get() = Paths.get(properties.persistence.publicSubmissionFtpOutPath)
    private val includeSecretKey: Boolean get() = properties.persistence.includeSecretKey
}
