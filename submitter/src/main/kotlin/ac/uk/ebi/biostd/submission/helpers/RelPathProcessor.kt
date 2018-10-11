package ac.uk.ebi.biostd.submission.helpers

import ebi.ac.uk.base.lastDigits

/**
 * Calculate the submission relative path. Patterns are generated to avoid
 */
class RelPathProcessor {

    fun getRelPath(accNo: AccNumber): String {
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
}
