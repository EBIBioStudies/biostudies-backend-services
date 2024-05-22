package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.nio.file.Path
import java.nio.file.Paths

internal class SubFolderResolver(
    private val properties: ApplicationProperties,
) : SubmissionFolderResolver {
    override fun getPublicSubFolder(submissionRelPath: String): Path {
        return publicSubPath.resolve(submissionRelPath)
    }

    override fun getPrivateSubFolder(
        secretKey: String,
        relPath: String,
    ): Path {
        return when (includeSecretKey) {
            true -> privateSubPath.resolve(secretKey.take(2)).resolve("${secretKey.substring(2)}/$relPath")
            else -> privateSubPath.resolve(relPath)
        }
    }

    private val publicSubPath: Path get() = Paths.get(properties.persistence.publicSubmissionsPath)
    private val privateSubPath: Path get() = Paths.get(properties.persistence.privateSubmissionsPath)
    private val includeSecretKey: Boolean get() = properties.persistence.includeSecretKey
}
