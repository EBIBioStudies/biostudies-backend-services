package ebi.ac.uk.util.regex

import arrow.core.Option

fun Regex.getGroup(input: String, group: Int) =
    Option.fromNullable(find(input)?.groups?.get(group)).map { it.value }
