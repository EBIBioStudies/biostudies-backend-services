package ebi.ac.uk.util.regex

import java.util.regex.Matcher

@Suppress("MagicNumber")
fun Matcher.firstGroup() = group(1)!!

@Suppress("MagicNumber")
fun Matcher.secondGroup() = group(2)!!

@Suppress("MagicNumber")
fun Matcher.thirdGroup() = group(3)!!
