package ebi.ac.uk.submission.processing.accno

import ac.uk.ebi.biostd.persistence.integration.SubmissionService
import arrow.core.Option
import arrow.core.getOrElse
import ebi.ac.uk.extended.exception.ProvideAccessNumber
import ebi.ac.uk.extended.exception.UserCanNotUpdateSubmit
import ebi.ac.uk.extended.processing.accno.extractAccessNumber
import ebi.ac.uk.extended.processing.accno.getPattern
import ebi.ac.uk.extended.processing.accno.isPattern
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User

const val DEFAULT_PATTERN = "!{S-BSST,}"
const val PATH_DIGITS = 3

/**
 * Calculate the accession number and relative path for the given submission.
 */
fun SubmissionService.getAccNo(submission: Submission, user: User): AccNumber {
    return when {
        isNew(submission.accNo) && canUserProvideAccNo(user).not() ->
            throw ProvideAccessNumber(user)
        canSubmit(submission.accNo, user).not() ->
            throw UserCanNotUpdateSubmit(submission, user)
        else ->
            calculateAccNo(submission)
    }
}

private fun SubmissionService.calculateAccNo(submission: Submission): AccNumber {
    return when {
        isPattern(submission.accNo) ->
            calculateAccNo(getPattern(submission.accNo))
        submission.accNo.isEmpty() ->
            calculateAccNo(getPatternOrDefault(getProjectAccPattern(submission)))
        else ->
            extractAccessNumber(submission.accNo)
    }
}

private fun SubmissionService.calculateAccNo(pattern: AccPattern) = AccNumber(pattern, getSequenceNextValue(pattern))

private fun getPatternOrDefault(pattern: Option<String>) = pattern.map { getPattern(it) }.getOrElse { getPattern(DEFAULT_PATTERN) }

