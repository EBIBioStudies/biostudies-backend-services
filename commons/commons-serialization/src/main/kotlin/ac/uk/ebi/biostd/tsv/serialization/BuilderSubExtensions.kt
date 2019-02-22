package ac.uk.ebi.biostd.tsv.serialization

import ebi.ac.uk.util.collections.ifNotEmpty

fun TsvBuilder.addSeparator() {
    append("\n")
}

fun TsvBuilder.addSubAccAndTags(accNo: String, tags: List<String>) {
    append("$ACC_NO_KEY\t$accNo")
    tags.ifNotEmpty {
        append("\t${tags.joinToString(separator = TAGS_SEPARATOR)}\n")
    }
}
