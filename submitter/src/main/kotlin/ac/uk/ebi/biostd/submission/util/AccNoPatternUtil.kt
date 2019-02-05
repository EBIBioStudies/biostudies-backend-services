package ac.uk.ebi.biostd.submission.util

import ac.uk.ebi.biostd.submission.exceptions.InvalidAccNoPattern
import ac.uk.ebi.biostd.submission.exceptions.InvalidPatternException
import arrow.core.getOrElse
import arrow.core.orElse
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.util.regex.firstGroup
import ebi.ac.uk.util.regex.match
import ebi.ac.uk.util.regex.secondGroup
import ebi.ac.uk.util.regex.thirdGroup
import java.util.regex.Matcher

private const val ACC_PATTERN = "\\!\\{%s\\}"
private const val EXPECTED_PATTERN = "([A-Z,-]*),([A-Z,-]*)"

private val ONLY_PREFIX = ACC_PATTERN.format("([A-Z,-]*),").toPattern()
private val ONLY_POSTFIX = ACC_PATTERN.format(",([A-Z,-]*)").toPattern()
private val PREFIX_POSTFIX = ACC_PATTERN.format(EXPECTED_PATTERN).toPattern()
private val EXTRACTION_PATTERN = "(\\D*)([0-9]+)(\\D*)".toPattern()

class AccNoPatternUtil {

    fun getPattern(accPattern: String) =
            getPrefixAccPattern(accPattern)
                    .orElse { getPostfixAccPattern(accPattern) }
                    .orElse { getPrefixPostfixPattern(accPattern) }
                    .getOrElse { throw InvalidPatternException(accPattern, EXPECTED_PATTERN) }

    /**
     * Determinate if the submission acc no is a pattern, which is determinate if match the @see [ACC_PATTERN]
     * expression.
     */
    fun isPattern(accNo: String) = ACC_PATTERN.format(".*").toPattern().matcher(accNo).matches()

    /**
     * Extract the @see [AccNumber] for the given accession string.
     */
    fun extractAccessNumber(accNo: String) = EXTRACTION_PATTERN.match(accNo)
            .map(::asAccNumber).getOrElse { throw InvalidAccNoPattern(accNo, EXTRACTION_PATTERN) }

    private fun asAccNumber(it: Matcher) =
            AccNumber(AccPattern(it.firstGroup(), it.thirdGroup()), it.secondGroup().toLong())

    private fun getPrefixAccPattern(accNo: String) =
            ONLY_PREFIX.match(accNo).map { AccPattern(prefix = it.firstGroup()) }

    private fun getPostfixAccPattern(accNo: String) =
            ONLY_POSTFIX.match(accNo).map { AccPattern(postfix = it.firstGroup()) }

    private fun getPrefixPostfixPattern(accNo: String) =
            PREFIX_POSTFIX.match(accNo).map { AccPattern(it.firstGroup(), it.secondGroup()) }
}
