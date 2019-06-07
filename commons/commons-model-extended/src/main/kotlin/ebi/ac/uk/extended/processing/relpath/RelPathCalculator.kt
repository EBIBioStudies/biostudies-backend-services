package ebi.ac.uk.extended.processing.relpath

import ebi.ac.uk.base.lastDigits
import ebi.ac.uk.extended.processing.accno.PATH_DIGITS
import ebi.ac.uk.model.AccNumber

@Suppress("MagicNumber")
class RelPathCalculator {

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
}
