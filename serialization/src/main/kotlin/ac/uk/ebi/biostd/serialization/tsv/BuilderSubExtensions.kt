package ac.uk.ebi.biostd.serialization.tsv

import ebi.ac.uk.funtions.asIsoDate

fun TsvBuilder.addSeparator() {
    append("\n")
}

fun TsvBuilder.addSubTitle(title: String) {
    with(TITLE_KEY, title)
}

fun TsvBuilder.addRootPath(rootPath: String?) = rootPath?.let { with(ROOT_PATH_KEY, it) }


fun TsvBuilder.addSubReleaseDate(rTime: Long) {
    with(RELEASE_DATE_KEY, asIsoDate(rTime).toString())
}

fun TsvBuilder.addSubAccAndTags(accNo: String, tags: List<String>) {
    append("$ACC_NO_KEY\t$accNo\t${tags.joinToString(separator = TAGS_SEPARATOR)}\n")
}


