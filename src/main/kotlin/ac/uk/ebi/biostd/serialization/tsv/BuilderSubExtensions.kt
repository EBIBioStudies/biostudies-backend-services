package ac.uk.ebi.biostd.serialization.tsv

import java.time.Instant

fun TsvBuilder.addSeparator() {
    append("\n")
}

fun TsvBuilder.addSubTitle(title: String) {
    with(titleKey, title)
}

fun TsvBuilder.addRootPath(rootPath: String) {
    with(rootPathKey, rootPath)
}

fun TsvBuilder.addSubReleaseDate(rTime: Long) {
    with(releaseDateKey, asIsoDate(rTime))
}

fun TsvBuilder.addSubAccAndTags(accNo: String, tags: List<String>) {
    append("$accNoKey\t$accNo\t${tags.joinToString(separator = tagsSeparator)}\n")
}

private fun asIsoDate(seconds: Long): String = Instant.ofEpochSecond(seconds).toString()
