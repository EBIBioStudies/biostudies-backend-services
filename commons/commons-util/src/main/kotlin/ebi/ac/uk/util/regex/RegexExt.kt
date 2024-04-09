package ebi.ac.uk.util.regex

fun Regex.findGroup(
    input: String,
    group: Int,
) = find(input)?.groups?.get(group)?.value?.let { if (it == "") null else it }

fun Regex.getGroup(
    input: String,
    group: Int = 0,
) = find(input)?.groups?.get(group)!!.value
