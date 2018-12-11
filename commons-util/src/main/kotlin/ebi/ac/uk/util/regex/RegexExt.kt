package ebi.ac.uk.util.regex

import ebi.ac.uk.base.toOption

fun Regex.findGroup(input: String, group: Int) = find(input)?.groups?.get(group)?.value.orEmpty().toOption()

fun Regex.getGroup(input: String, group: Int = 0) = find(input)?.groups?.get(group)!!.value