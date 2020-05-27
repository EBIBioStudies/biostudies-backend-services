package uk.ac.ebi.extended.serialization.constants

private const val FILE = "file"
private const val FILES_TABLE = "filesTable"
private const val LINK = "link"
private const val LINKS_TABLE = "linksTable"
private const val SECTION = "section"
private const val SECTIONS_TABLE = "sectionsTable"

sealed class ExtType(val type: String) {
    object File : ExtType(FILE)
    object FilesTable : ExtType(FILES_TABLE)
    object Link : ExtType(LINK)
    object LinksTable : ExtType(LINKS_TABLE)
    object Section : ExtType(SECTION)
    object SectionsTable : ExtType(SECTIONS_TABLE)
}
