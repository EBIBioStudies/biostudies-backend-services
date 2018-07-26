package ac.uk.ebi.biostd.serialization.tsv

import java.time.Instant

fun TsvBuilder.addSeparator() {
    append("\n")
}

fun TsvBuilder.addSubTitle(title: String) {
    with(TITLE_KEY, title)
}

fun TsvBuilder.addRootPath(rootPath: String) {
    with(ROOT_PATH_KEY, rootPath)
}

fun TsvBuilder.addSubReleaseDate(rTime: Long) {
    with(RELEASE_DATE_KEY, asIsoDate(rTime))
}

fun TsvBuilder.addSubAccAndTags(accNo: String, tags: List<String>) {
    append("$ACC_NO_KEY\t$accNo\t${tags.joinToString(separator = TAGS_SEPARATOR)}\n")
}

private fun asIsoDate(seconds: Long): String = Instant.ofEpochSecond(seconds).toString()
