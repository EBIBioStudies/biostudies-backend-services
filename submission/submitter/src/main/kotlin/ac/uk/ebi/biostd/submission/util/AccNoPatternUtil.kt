package ac.uk.ebi.biostd.submission.util

import ac.uk.ebi.biostd.submission.exceptions.InvalidPatternException
import arrow.core.getOrElse
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.util.regex.firstGroup
import ebi.ac.uk.util.regex.match
import ebi.ac.uk.util.regex.secondGroup
import java.util.regex.Matcher

private const val ACC_PATTERN = "\\!\\{%s\\}"
private const val EXPECTED_PATTERN = "([A-Z,-]*)"

class AccNoPatternUtil {
    private val prefix = ACC_PATTERN.format("([A-Z,-]*)").toPattern()
    private val extractionPattern = "^(.*?)(\\d+)\$".toPattern()

    fun getPattern(accPattern: String): String =
        getPrefixAccPattern(accPattern).getOrElse { throw InvalidPatternException(accPattern, EXPECTED_PATTERN) }

    /**
     * Checks if the submission accession number is a pattern, based on whether or not it matches the @see [ACC_PATTERN]
     * expression.
     */
    fun isPattern(accNo: String): Boolean = ACC_PATTERN.format(".*").toPattern().matcher(accNo).matches()

    /**
     * Extracts the @see [AccNumber] for the given accession string.
     */
    fun toAccNumber(accNo: String): AccNumber =
        extractionPattern
            .match(accNo)
            .map(::asAccNumber)
            .getOrElse { AccNumber((accNo)) }

    private fun asAccNumber(it: Matcher) = AccNumber(it.firstGroup(), it.secondGroup())

    private fun getPrefixAccPattern(accNo: String) = prefix.match(accNo).map { it.firstGroup() }
}
