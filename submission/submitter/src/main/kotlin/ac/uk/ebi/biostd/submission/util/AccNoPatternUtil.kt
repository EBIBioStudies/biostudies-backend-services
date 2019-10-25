package ac.uk.ebi.biostd.submission.util

import ac.uk.ebi.biostd.submission.exceptions.InvalidAccNoPattern
import ac.uk.ebi.biostd.submission.exceptions.InvalidPatternException
import arrow.core.getOrElse
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.util.regex.firstGroup
import ebi.ac.uk.util.regex.match
import ebi.ac.uk.util.regex.secondGroup
import ebi.ac.uk.util.regex.thirdGroup
import java.util.regex.Matcher

private const val ACC_PATTERN = "\\!\\{%s\\}"
private const val EXPECTED_PATTERN = "([A-Z,-]*)"

class AccNoPatternUtil {
    private val prefix = ACC_PATTERN.format("([A-Z,-]*)").toPattern()
    private val extractionPattern = "(\\D*)([0-9]+)(\\D*)".toPattern()

    fun getPattern(accPattern: String) =
        getPrefixAccPattern(accPattern).getOrElse { throw InvalidPatternException(accPattern, EXPECTED_PATTERN) }

    /**
     * Checks if the submission accession number is a pattern, based on whether or not it matches the @see [ACC_PATTERN]
     * expression.
     */
    fun isPattern(accNo: String) = ACC_PATTERN.format(".*").toPattern().matcher(accNo).matches()

    /**
     * Extracts the @see [AccNumber] for the given accession string.
     */
    fun extractAccessNumber(accNo: String) =
        extractionPattern
            .match(accNo)
            .map(::asAccNumber)
            .getOrElse { throw InvalidAccNoPattern(accNo, extractionPattern) }

    private fun asAccNumber(it: Matcher) =
            AccNumber(AccPattern(it.firstGroup(), it.thirdGroup()), it.secondGroup().toLong())

    private fun getPrefixAccPattern(accNo: String) =
        prefix.match(accNo).map { AccPattern(prefix = it.firstGroup()) }
}
