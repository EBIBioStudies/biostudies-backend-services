package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.submission.exceptions.ProvideAccessNumber
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitProjectsException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.base.lastDigits
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

const val DEFAULT_PATTERN = "!{S-BSST}"
const val PATH_DIGITS = 3

class AccNoService(
    private val context: PersistenceContext,
    private val patternUtil: AccNoPatternUtil,
    private val privilegesService: IUserPrivilegesService
) {
    @Suppress("ThrowsCount")
    fun getAccNo(request: AccNoServiceRequest): AccNumber {
        val submitter = request.user.email
        val accNo = request.accNo
        val project = request.parentAccNo

        when {
            context.isNew(request.accNo) -> {
                if (accNo.isNotEmpty() && privilegesService.canProvideAccNo(submitter).not())
                    throw ProvideAccessNumber(submitter)
                if (project != null && privilegesService.canSubmitToProject(submitter, project).not())
                    throw UserCanNotSubmitProjectsException(submitter)

                return if (accNo.isEmpty())
                    calculateAccNo(getPatternOrDefault(request.parentPattern))
                else AccNumber(request.accNo, null)
            }
            else -> {
                val tags = context.getAccessTags(request.accNo).filterNot { it == SubFields.PUBLIC_ACCESS_TAG.value }
                if (project != null && privilegesService.canSubmitToProject(submitter, project).not())
                    throw UserCanNotSubmitProjectsException(request.user.email)

                if (privilegesService.canResubmit(submitter, context.getAuthor(accNo), tags).not())
                    throw UserCanNotUpdateSubmit(request.accNo, submitter)

                return AccNumber(request.accNo, null)
            }
        }
    }

    private fun calculateAccNo(prefix: String) = AccNumber(prefix, context.getSequenceNextValue(prefix))

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
    val user: User,
    val accNo: String,
    val subType: String,
    val parentAccNo: String? = null,
    val parentPattern: String? = null
)
