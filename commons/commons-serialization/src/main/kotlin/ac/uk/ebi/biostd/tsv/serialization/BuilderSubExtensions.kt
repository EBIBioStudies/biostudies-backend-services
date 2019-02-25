package ac.uk.ebi.biostd.tsv.serialization

fun TsvBuilder.addSeparator() {
    append("\n")
}

fun TsvBuilder.addSubAccAndTags(accNo: String, tags: List<String>) {
    append("$ACC_NO_KEY\t$accNo")

    if (tags.isEmpty()) addSeparator()
    else append("\t${tags.joinToString(separator = TAGS_SEPARATOR)}\n")
}
