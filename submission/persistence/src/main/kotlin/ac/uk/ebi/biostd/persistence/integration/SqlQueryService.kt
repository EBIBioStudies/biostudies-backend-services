package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.exception.ProjectNotFoundException
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ebi.ac.uk.model.constants.SubFields
import java.time.OffsetDateTime

@Suppress("TooManyFunctions")
class SubmissionSqlQueryService(
    private val subRepository: SubmissionDataRepository
) : SubmissionQueryService {

    override fun getParentAccPattern(parentAccNo: String) =
        getParentSubmission(parentAccNo)
            .let { parent -> parent.attributes.firstOrNull { it.name == SubFields.ACC_NO_TEMPLATE.value } }
            .let { it?.value }

    override fun isNew(accNo: String): Boolean = existByAccNo(accNo).not()

    override fun getSecret(accNo: String): String = getSubmission(accNo).secretKey

    override fun getAccessTags(accNo: String): List<String> = getSubmission(accNo).accessTags.map { it.name }

    override fun getReleaseTime(accNo: String): OffsetDateTime? = getSubmission(accNo).releaseTime

    override fun existByAccNo(accNo: String): Boolean = subRepository.existsByAccNo(accNo)

    override fun findCreationTime(accNo: String): OffsetDateTime? = findSubmission(accNo)?.creationTime

    override fun getAuthor(accNo: String): String = getSubmission(accNo).owner.email

    private fun getSubmission(accNo: String) = subRepository.getByAccNoAndVersionGreaterThan(accNo, 0)

    private fun findSubmission(accNo: String) = subRepository.findByAccNoAndVersionGreaterThan(accNo, 0)

    private fun getParentSubmission(parentAccNo: String) =
        subRepository.findByAccNoAndVersionGreaterThan(parentAccNo) ?: throw ProjectNotFoundException(parentAccNo)
}

interface SubmissionQueryService {
    fun getParentAccPattern(parentAccNo: String): String?

    fun isNew(accNo: String): Boolean

    fun getSecret(accNo: String): String

    fun getAccessTags(accNo: String): List<String>

    fun getReleaseTime(accNo: String): OffsetDateTime?

    fun existByAccNo(accNo: String): Boolean

    fun findCreationTime(accNo: String): OffsetDateTime?

    fun getAuthor(accNo: String): String
}
