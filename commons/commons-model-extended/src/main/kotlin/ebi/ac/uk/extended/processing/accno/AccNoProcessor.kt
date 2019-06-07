package ebi.ac.uk.extended.processing.accno

import arrow.core.Option
import arrow.core.getOrElse
import ebi.ac.uk.extended.exception.ProvideAccessNumber
import ebi.ac.uk.extended.exception.UserCanNotUpdateSubmit
import ebi.ac.uk.extended.integration.SubmissionService
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User

const val DEFAULT_PATTERN = "!{S-BSST,}"
const val PATH_DIGITS = 3

/**
 * Calculate the accession number and relative path for the given submission.
 */
class AccNoProcessor(
    private val patternUtil: AccNoPatternUtil = AccNoPatternUtil(),
    private val submissionService: SubmissionService
) {

    fun getAccNo(submission: Submission, user: User): AccNumber {
        return when {
            submissionService.isNew(submission.accNo) && submissionService.canUserProvideAccNo(user).not() ->
                throw ProvideAccessNumber(user)
            submissionService.canSubmit(submission.accNo, user).not() ->
                throw UserCanNotUpdateSubmit(submission, user)
            else ->
                calculateAccNo(submission)
        }
    }

    private fun calculateAccNo(submission: Submission): AccNumber {
        return when {
            patternUtil.isPattern(submission.accNo) ->
                calculateAccNo(patternUtil.getPattern(submission.accNo))
            submission.accNo.isEmpty() ->
                calculateAccNo(getPatternOrDefault(submissionService.getProjectAccPattern(submission)))
            else ->
                patternUtil.extractAccessNumber(submission.accNo)
        }
    }

    private fun calculateAccNo(pattern: AccPattern) = AccNumber(pattern, submissionService.getSequenceNextValue(pattern))

    private fun getPatternOrDefault(pattern: Option<String>) =
        pattern.map { patternUtil.getPattern(it) }.getOrElse { patternUtil.getPattern(DEFAULT_PATTERN) }
}
