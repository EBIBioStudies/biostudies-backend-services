package ac.uk.ebi.biostd.submission.util

import ac.uk.ebi.biostd.submission.processors.ACC_PATTERN
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.orElse
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.util.regex.firstGroup
import ebi.ac.uk.util.regex.secondGroup
import ebi.ac.uk.util.regex.thirdGroup
import java.util.regex.Matcher
import java.util.regex.Pattern

private val onlyPrefix = ACC_PATTERN.format("([A-Z,-]*),").toPattern()
private val onlyPostfix = ACC_PATTERN.format(",([A-Z,-]*)").toPattern()
private val prefixPostfix = ACC_PATTERN.format("([A-Z,-]*),([A-Z,-]*)").toPattern()
private val extractionPattern = "(\\w)*([0-9]+)(.*)".toPattern()

class PatternProcessor {

    fun generateAccNumber(accPattern: String, sequenceFunction: (String) -> Long) =
        getPrefixAccPattern(accPattern)
            .orElse { getPostfixAccPattern(accPattern) }
            .orElse { getPrefixPostfixPattern(accPattern) }
            .map { pattern -> AccNumber(pattern, sequenceFunction(pattern.toString())) }
            .getOrElse { throw IllegalStateException() }

    fun extractAccessNumber(accNo: String) = tryToGet(accNo, extractionPattern) {
        AccNumber(PrefixPostfix(it.firstGroup(), it.secondGroup()), it.thirdGroup().toLong())
    }.getOrElse { throw IllegalStateException() }

    private fun getPrefixAccPattern(accNo: String) =
        tryToGet(accNo, onlyPrefix) { PrefixPostfix(prefix = it.firstGroup()) }

    private fun getPostfixAccPattern(accNo: String) =
        tryToGet(accNo, onlyPostfix) { PrefixPostfix(postfix = it.firstGroup()) }

    private fun getPrefixPostfixPattern(accNo: String) =
        tryToGet(accNo, prefixPostfix) { PrefixPostfix(it.firstGroup(), it.secondGroup()) }

    private fun <T> tryToGet(accNo: String, pattern: Pattern, function: (Matcher) -> T): Option<T> {
        val matcher = pattern.matcher(accNo)
        return when {
            matcher.matches() -> Option.just(function(matcher))
            else -> Option.empty()
        }
    }
}

class AccNumber(val pattern: PrefixPostfix, val numericValue: Long) {

    override fun toString() = "${pattern.prefix}$numericValue${pattern.postfix}"
}

class PrefixPostfix(val prefix: String = EMPTY, val postfix: String = EMPTY) {

    override fun toString() = "$prefix,$postfix"
}
