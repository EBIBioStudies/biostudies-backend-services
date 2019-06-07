package ebi.ac.uk.extended.processing.accno

import arrow.core.getOrElse
import arrow.core.orElse
import ebi.ac.uk.extended.exception.InvalidAccNoPattern
import ebi.ac.uk.extended.exception.InvalidPatternException
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.util.regex.firstGroup
import ebi.ac.uk.util.regex.match
import ebi.ac.uk.util.regex.secondGroup
import ebi.ac.uk.util.regex.thirdGroup
import java.util.regex.Matcher

private const val ACC_PATTERN = "\\!\\{%s\\}"
private const val EXPECTED_PATTERN = "([A-Z,-]*),([A-Z,-]*)"

private val onlyPrefix = ACC_PATTERN.format("([A-Z,-]*),").toPattern()
private val onlyPostfix = ACC_PATTERN.format(",([A-Z,-]*)").toPattern()
private val prefixPostfix = ACC_PATTERN.format(EXPECTED_PATTERN).toPattern()
private val extractionPattern = "(\\D*)([0-9]+)(\\D*)".toPattern()

class AccNoPatternUtil {

    fun getPattern(accPattern: String) =
        getPrefixAccPattern(accPattern)
            .orElse { getPostfixAccPattern(accPattern) }
            .orElse { getPrefixPostfixPattern(accPattern) }
            .getOrElse { throw InvalidPatternException(accPattern, EXPECTED_PATTERN) }

    /**
     * Checks if the submission accession number is a pattern, based on whether or not it matches the @see [ACC_PATTERN]
     * expression.
     */
    fun isPattern(accNo: String) = ACC_PATTERN.format(".*").toPattern().matcher(accNo).matches()

    /**
     * Extracts the @see [AccNumber] for the given accession string.
     */
    fun extractAccessNumber(accNo: String) = extractionPattern.match(accNo)
        .map(::asAccNumber).getOrElse { throw InvalidAccNoPattern(accNo, extractionPattern) }

    private fun asAccNumber(it: Matcher) =
        AccNumber(AccPattern(it.firstGroup(), it.thirdGroup()), it.secondGroup().toLong())

    private fun getPrefixAccPattern(accNo: String) =
        onlyPrefix.match(accNo).map { AccPattern(prefix = it.firstGroup()) }

    private fun getPostfixAccPattern(accNo: String) =
        onlyPostfix.match(accNo).map { AccPattern(postfix = it.firstGroup()) }

    private fun getPrefixPostfixPattern(accNo: String) =
        prefixPostfix.match(accNo).map { AccPattern(it.firstGroup(), it.secondGroup()) }
}
