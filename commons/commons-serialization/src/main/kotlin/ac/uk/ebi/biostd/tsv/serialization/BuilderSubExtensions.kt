package ac.uk.ebi.biostd.tsv.serialization

internal fun TsvBuilder.addSeparator() {
    append("\n")
}

internal fun TsvBuilder.addSubAccAndTags(accNo: String, tags: List<String>) {
    append("$ACC_NO_KEY\t$accNo")

    if (tags.isEmpty()) addSeparator()
    else append("\t${tags.joinToString(separator = TAGS_SEPARATOR)}\n")
}
