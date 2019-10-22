package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.exceptions.ProvideAccessNumber
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import arrow.core.Option
import arrow.core.getOrElse
import ebi.ac.uk.base.lastDigits
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

const val DEFAULT_PATTERN = "!{S-BSST,}"
const val PATH_DIGITS = 3

/**
 * Calculate the accession number and relative path for the given submission.
 */
class AccNoProcessor(
    private val userPrivilegesService: IUserPrivilegesService,
    private val patternUtil: AccNoPatternUtil
) : SubmissionProcessor {
    override fun process(submission: ExtendedSubmission, context: PersistenceContext) {
        val accNo = getAccNo(submission, context)

        submission.accNo = accNo.toString()
        submission.relPath = getRelPath(accNo)
    }

    private fun getAccNo(submission: ExtendedSubmission, context: PersistenceContext): AccNumber {
        return when {
            context.isNew(submission.accNo) && userPrivilegesService.canProvideAccNo(submission.user.email).not() ->
                throw ProvideAccessNumber(submission.user)
            userPrivilegesService.canResubmit(
                submission.user.email, submission.user, submission.attachTo, submission.accessTags).not() ->
                throw UserCanNotUpdateSubmit(submission)
            else ->
                calculateAccNo(submission, context)
        }
    }

    private fun calculateAccNo(submission: ExtendedSubmission, context: PersistenceContext): AccNumber {
        return when {
            patternUtil.isPattern(submission.accNo) ->
                calculateAccNo(patternUtil.getPattern(submission.accNo), context)
            submission.accNo.isEmpty() ->
                calculateAccNo(getPatternOrDefault(context.getParentAccPattern(submission)), context)
            else ->
                patternUtil.extractAccessNumber(submission.accNo)
        }
    }

    private fun calculateAccNo(pattern: AccPattern, context: PersistenceContext) =
        AccNumber(pattern, context.getSequenceNextValue(pattern))

    @Suppress("MagicNumber")
    internal fun getRelPath(accNo: AccNumber): String {
        val prefix = accNo.pattern.prefix
        val postfix = accNo.pattern.postfix
        val value = accNo.numericValue

        return when {
            accNo.numericValue < 99 ->
                "$prefix/${prefix}0-99$postfix/$prefix$value$postfix".removePrefix("/")
            else ->
                "$prefix/${prefix}xxx${value.lastDigits(PATH_DIGITS)}$postfix/$prefix$value$postfix".removePrefix("/")
        }
    }

    private fun getPatternOrDefault(pattern: Option<String>) =
        pattern.map { patternUtil.getPattern(it) }.getOrElse { patternUtil.getPattern(DEFAULT_PATTERN) }
}
