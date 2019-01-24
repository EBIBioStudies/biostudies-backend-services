package ebi.ac.uk.util.regex

import java.util.regex.Matcher

fun Matcher.firstGroup() = group(1)

fun Matcher.secondGroup() = group(2)

fun Matcher.thirdGroup() = group(3)
