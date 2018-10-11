package ac.uk.ebi.biostd.submission.helpers

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import arrow.core.Option
import arrow.core.getOrElse
import ebi.ac.uk.model.ISubmission

const val ACC_PATTERN = "\\!\\{%s\\}"
const val DEFAULT_PATTERN = "!{S-BSST,}"

class AccNoProcessor(private val patternExtractor: PatternProcessor = PatternProcessor()) {

    fun getAccNo(submission: ISubmission, context: PersistenceContext): AccNumber {

        return when {
            submission.accNo.isEmpty() -> getPattern(context.getParentAccPattern(submission), context::getSequenceNextValue)
            isPattern(submission.accNo) -> patternExtractor.generateAccNumber(submission.accNo, context::getSequenceNextValue)
            else -> patternExtractor.extractAccessNumber(submission.accNo)
        }
    }

    private fun isPattern(accNo: String) = ACC_PATTERN.format(".*").toPattern().matcher(accNo).matches()

    private fun getPattern(pattern: Option<String>, sequenceFunction: (String) -> Long) =
            pattern.map { patternExtractor.generateAccNumber(it, sequenceFunction) }
                    .getOrElse { patternExtractor.generateAccNumber(DEFAULT_PATTERN, sequenceFunction) }

}
