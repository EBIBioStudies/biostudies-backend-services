package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.util.AccNumber
import ac.uk.ebi.biostd.submission.util.PatternProcessor
import ac.uk.ebi.biostd.submission.model.PersistenceContext
import arrow.core.Option
import arrow.core.getOrElse
import ebi.ac.uk.base.lastDigits
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User

const val ACC_PATTERN = "\\!\\{%s\\}"
const val DEFAULT_PATTERN = "!{S-BSST,}"

class AccNoProcessor(private val patternExtractor: PatternProcessor = PatternProcessor()) : SubmissionProcessor {

    override fun process(user: User, submission: ExtendedSubmission, persistenceContext: PersistenceContext) {
        val accNo = getAccNo(submission, persistenceContext)

        submission.accNo = accNo.toString()
        submission.relPath = getRelPath(accNo)
    }

    private fun getAccNo(submission: ExtendedSubmission, context: PersistenceContext) = when {
        submission.accNo.isEmpty() -> getPattern(context.getParentAccPattern(submission), context::getSequenceNextValue)
        isPattern(submission.accNo) -> patternExtractor.generateAccNumber(submission.accNo, context::getSequenceNextValue)
        else -> patternExtractor.extractAccessNumber(submission.accNo)
    }

    internal fun getRelPath(accNo: AccNumber): String {
        val prefix = accNo.pattern.prefix
        val postfix = accNo.pattern.postfix
        val value = accNo.numericValue

        return when {
            accNo.numericValue < 99 ->
                "$prefix/${prefix}0-99$postfix/$prefix$value$postfix".removePrefix("/")
            else ->
                "$prefix/${prefix}xxx${value.lastDigits(3)}$postfix/$prefix$value$postfix".removePrefix("/")
        }
    }

    private fun isPattern(accNo: String) = ACC_PATTERN.format(".*").toPattern().matcher(accNo).matches()

    private fun getPattern(pattern: Option<String>, sequenceFunction: (String) -> Long) =
            pattern.map { patternExtractor.generateAccNumber(it, sequenceFunction) }
                    .getOrElse { patternExtractor.generateAccNumber(DEFAULT_PATTERN, sequenceFunction) }

}
