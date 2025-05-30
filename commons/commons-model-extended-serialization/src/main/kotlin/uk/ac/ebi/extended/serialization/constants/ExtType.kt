package uk.ac.ebi.extended.serialization.constants

import uk.ac.ebi.extended.serialization.exception.InvalidExtTypeException

private const val REQUEST_FILE = "requestFile"
private const val NFS_FILE = "nfsFile"
private const val FIRE_FILE = "fireFile"
private const val FILES_TABLE = "filesTable"
private const val LINK = "link"
private const val LINKS_TABLE = "linksTable"
private const val SECTION = "section"
private const val SECTIONS_TABLE = "sectionsTable"

sealed class ExtType(
    val type: String,
) {
    object RequestFile : ExtType(REQUEST_FILE)

    object NfsFile : ExtType(NFS_FILE)

    object FireFile : ExtType(FIRE_FILE)

    object FilesTable : ExtType(FILES_TABLE)

    object Link : ExtType(LINK)

    object LinksTable : ExtType(LINKS_TABLE)

    object Section : ExtType(SECTION)

    object SectionsTable : ExtType(SECTIONS_TABLE)

    companion object {
        fun valueOf(type: String): ExtType =
            when (type) {
                NFS_FILE -> NfsFile
                FIRE_FILE -> FireFile
                REQUEST_FILE -> RequestFile
                FILES_TABLE -> FilesTable
                LINK -> Link
                LINKS_TABLE -> LinksTable
                SECTION -> Section
                SECTIONS_TABLE -> SectionsTable
                else -> throw InvalidExtTypeException(type)
            }
    }
}
