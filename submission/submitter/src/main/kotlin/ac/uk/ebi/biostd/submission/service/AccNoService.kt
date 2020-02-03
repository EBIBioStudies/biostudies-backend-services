package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.submission.exceptions.ProvideAccessNumber
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import arrow.core.Option
import arrow.core.getOrElse
import ebi.ac.uk.base.lastDigits
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

const val DEFAULT_PATTERN = "!{S-BSST}"
const val PATH_DIGITS = 3

class AccNoService(
    private val context: PersistenceContext,
    private val patternUtil: AccNoPatternUtil,
    private val userPrivilegesService: IUserPrivilegesService
) {
    fun getAccNo(submission: ExtendedSubmission): AccNumber = when {
        submission.accNo.isNotEmpty() &&
            context.isNew(submission.accNo) &&
            userPrivilegesService.canProvideAccNo(submission.user.email).not() ->
            throw ProvideAccessNumber(submission.user)
        userPrivilegesService.canResubmit(
            submission.user.email, submission.user, submission.attachTo, submission.accessTags).not() ->
            throw UserCanNotUpdateSubmit(submission)
        else ->
            calculateAccNo(submission)
    }

    private fun calculateAccNo(submission: ExtendedSubmission): AccNumber {
        return when {
            submission.accNo.isEmpty() ->
                calculateAccNo(getPatternOrDefault(context.getParentAccPattern(submission)))
            patternUtil.isPattern(submission.accNo) ->
                calculateAccNo(patternUtil.getPattern(submission.accNo))
            else -> patternUtil.toAccNumber(submission.accNo)
        }
    }

    private fun calculateAccNo(prefix: String) =
        AccNumber(prefix, context.getSequenceNextValue(prefix))

    @Suppress("MagicNumber")
    internal fun getRelPath(accNo: AccNumber): String {
        val prefix = accNo.prefix
        val value = accNo.numericValue

        return when {
            accNo.numericValue < 99 ->
                "$prefix/${prefix}0-99/$prefix$value".removePrefix("/")
            else ->
                "$prefix/${prefix}xxx${value.lastDigits(PATH_DIGITS)}/$prefix$value".removePrefix("/")
        }
    }

    private fun getPatternOrDefault(pattern: Option<String>) =
        pattern.map { patternUtil.getPattern(it) }.getOrElse { patternUtil.getPattern(DEFAULT_PATTERN) }
}
