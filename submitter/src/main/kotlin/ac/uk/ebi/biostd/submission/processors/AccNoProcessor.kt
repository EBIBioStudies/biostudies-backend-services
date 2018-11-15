package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.exceptions.InvalidSecurityException
import ac.uk.ebi.biostd.submission.util.AccNumber
import ac.uk.ebi.biostd.submission.util.PatternProcessor
import arrow.core.Option
import arrow.core.getOrElse
import ebi.ac.uk.base.lastDigits
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.persistence.PersistenceContext

const val ACC_PATTERN = "\\!\\{%s\\}"
const val DEFAULT_PATTERN = "!{S-BSST,}"
const val VALUE_PATH_DIGITS = 3

/**
 * Calculate the accession number and relative path for the given submission.
 */
class AccNoProcessor(private val patternExtractor: PatternProcessor = PatternProcessor()) : SubmissionProcessor {

    override fun process(submission: ExtendedSubmission, persistenceContext: PersistenceContext) {
        val accNo = getAccNo(submission, persistenceContext)

        submission.accNo = accNo.toString()
        submission.relPath = getRelPath(accNo)
    }

    private fun getAccNo(submission: ExtendedSubmission, context: PersistenceContext): AccNumber {
        return when {
            submission.accNo.isEmpty() ->
                getPattern(context.getParentAccPattern(submission), context::getSequenceNextValue)
            context.canUserProvideAccNo(submission.user) ->
                throw InvalidSecurityException()
            context.canSubmit(submission.accNo, submission.user) ->
                throw InvalidSecurityException()
            isPattern(submission.accNo) ->
                patternExtractor.generateAccNumber(submission.accNo, context::getSequenceNextValue)
            else ->
                patternExtractor.extractAccessNumber(submission.accNo)
        }
    }

    internal fun getRelPath(accNo: AccNumber): String {
        val prefix = accNo.pattern.prefix
        val postfix = accNo.pattern.postfix
        val value = accNo.numericValue

        return when {
            accNo.numericValue < 99 ->
                "$prefix/${prefix}0-99$postfix/$prefix$value$postfix".removePrefix("/")
            else ->
                "$prefix/${prefix}xxx${value.lastDigits(VALUE_PATH_DIGITS)}$postfix/$prefix$value$postfix".removePrefix("/")
        }
    }

    private fun isPattern(accNo: String) = ACC_PATTERN.format(".*").toPattern().matcher(accNo).matches()

    private fun getPattern(pattern: Option<String>, sequenceFunction: (String) -> Long) =
        pattern.map { patternExtractor.generateAccNumber(it, sequenceFunction) }
            .getOrElse { patternExtractor.generateAccNumber(DEFAULT_PATTERN, sequenceFunction) }
}
