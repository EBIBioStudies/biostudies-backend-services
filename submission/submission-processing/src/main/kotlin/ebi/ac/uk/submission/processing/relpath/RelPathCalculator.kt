package ebi.ac.uk.submission.processing.relpath

import ebi.ac.uk.base.lastDigits
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.submission.processing.accno.PATH_DIGITS

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

