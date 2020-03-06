package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.submission.exceptions.InvalidProjectAccNoException
import ac.uk.ebi.biostd.submission.exceptions.ProvideAccessNumber
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitProjectsException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.base.lastDigits
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

const val DEFAULT_PATTERN = "!{S-BSST}"
const val PATH_DIGITS = 3

class AccNoService(
    private val context: PersistenceContext,
    private val patternUtil: AccNoPatternUtil,
    private val privilegesService: IUserPrivilegesService
) {
    @Suppress("ThrowsCount", "ReturnCount")
    fun getAccNo(request: AccNoServiceRequest): AccNumber {
        val (type, submitter, accNo, project, projectPattern) = request

        when {
            type == PROJECT_TYPE -> {
                require(accNo.isNotBlank()) { throw InvalidProjectAccNoException() }
                return AccNumber(accNo!!)
            }
            accNo == null || context.isNew(accNo) -> {
                if (accNo != null && privilegesService.canProvideAccNo(submitter).not())
                    throw ProvideAccessNumber(submitter)
                if (project != null && privilegesService.canSubmitToProject(submitter, project).not())
                    throw UserCanNotSubmitProjectsException(submitter)

                return accNo?.let { patternUtil.toAccNumber(it) } ?: calculateAccNo(getPatternOrDefault(projectPattern))
            }
            else -> {
                if (project != null && privilegesService.canSubmitToProject(submitter, project).not())
                    throw UserCanNotSubmitProjectsException(submitter)

                if (privilegesService.canResubmit(submitter, accNo).not())
                    throw UserCanNotUpdateSubmit(accNo, submitter)

                return patternUtil.toAccNumber(accNo)
            }
        }
    }

    private fun calculateAccNo(pattern: String) = AccNumber(pattern, context.getSequenceNextValue(pattern))

    @Suppress("MagicNumber")
    internal fun getRelPath(accNo: AccNumber): String {
        val prefix = accNo.prefix
        val value = accNo.numericValue

        return when {
            value == null ->
                prefix.removePrefix("/")
            value < 99 ->
                "$prefix/${prefix}0-99/$prefix$value".removePrefix("/")
            else ->
                "$prefix/${prefix}xxx${value.lastDigits(PATH_DIGITS)}/$prefix$value".removePrefix("/")
        }
    }

    private fun getPatternOrDefault(parentPattern: String?) = when (parentPattern) {
        null -> patternUtil.getPattern(DEFAULT_PATTERN)
        else -> patternUtil.getPattern(parentPattern)
    }
}

data class AccNoServiceRequest(
    val type: String,
    val submitter: String,
    val accNo: String? = null,
    val project: String? = null,
    val projectPattern: String? = null
)
