package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.submission.exceptions.ProvideAccessNumber
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.base.lastDigits
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.User
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

const val DEFAULT_PATTERN = "!{S-BSST}"
const val PATH_DIGITS = 3

class AccNoService(
    private val context: PersistenceContext,
    private val patternUtil: AccNoPatternUtil,
    private val userPrivilegesService: IUserPrivilegesService
) {
    fun getAccNo(request: AccNoServiceRequest): AccNumber = when {
        request.accNo.isNotEmpty() &&
            context.isNew(request.accNo) ->
            if (userPrivilegesService.canProvideAccNo(request.user.email).not())
                throw ProvideAccessNumber(request.user.email)
            else AccNumber(request.accNo, null)
        // TODO differentiate between user and author
        userPrivilegesService.canResubmit(
            request.user.email, request.user.email, request.parentAccNo, request.accessTags).not() ->
            throw UserCanNotUpdateSubmit(request.accNo, request.user.email)
        else -> calculateAccNo(request)
    }

    private fun calculateAccNo(request: AccNoServiceRequest): AccNumber = when {
        request.accNo.isEmpty() -> calculateAccNo(getPatternOrDefault(request))
        patternUtil.isPattern(request.accNo) -> calculateAccNo(patternUtil.getPattern(request.accNo))
        else -> patternUtil.toAccNumber(request.accNo)
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

    private fun getPatternOrDefault(request: AccNoServiceRequest) = when (request.parentAccNo) {
        null -> patternUtil.getPattern(DEFAULT_PATTERN)
        else -> patternUtil.getPattern(request.parentPattern!!)
    }
}

data class AccNoServiceRequest(
    val user: User,
    val accNo: String,
    val accessTags: List<String>,
    val parentAccNo: String? = null,
    val parentPattern: String? = null
)
