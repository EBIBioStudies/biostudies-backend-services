package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.paths.SubmissionFolderResolver
import java.time.OffsetDateTime

@Suppress("TooManyFunctions")
class SubmissionSqlQueryService(
    private val subRepository: SubmissionDataRepository,
    private val accessTagDataRepo: AccessTagDataRepo,
    private val folderResolver: SubmissionFolderResolver
) : SubmissionQueryService {
    override fun getParentAccPattern(parentAccNo: String): String? {
        return subRepository.getBasicWithAttributes(parentAccNo)
            .attributes.firstOrNull { it.name == SubFields.ACC_NO_TEMPLATE.value }
            ?.value
    }

    override fun isNew(accNo: String): Boolean = existByAccNo(accNo).not()

    override fun getSecret(accNo: String): String? = getLatestSubmitted(accNo)?.secretKey

    override fun getAccessTags(accNo: String) = accessTagDataRepo.findBySubmissionsAccNo(accNo).map { it.name }

    override fun getReleaseTime(accNo: String): OffsetDateTime? = getSubmission(accNo).releaseTime

    override fun existByAccNo(accNo: String): Boolean = subRepository.existsByAccNo(accNo)

    override fun findCreationTime(accNo: String): OffsetDateTime? = find(accNo)?.creationTime

    override fun getCurrentFolder(accNo: String) = find(accNo)?.let { folderResolver.getSubFolder(it.relPath).toFile() }

    override fun getOwner(accNo: String): String? = getLatestSubmitted(accNo)?.owner?.email

    private fun getSubmission(accNo: String) = subRepository.getBasic(accNo)

    private fun getLatestSubmitted(accNo: String) = subRepository.getLastVersion(accNo)

    private fun find(accNo: String) = subRepository.findBasic(accNo)
}
