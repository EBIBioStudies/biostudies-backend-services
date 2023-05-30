package uk.ac.ebi.io.builder

import ebi.ac.uk.extended.mapping.from.toExtAttributes
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FILES_RESERVED_ATTRS
import java.io.File

internal fun createFile(path: String, file: File, attributes: List<Attribute>): NfsFile {
    return NfsFile(
        filePath = path,
        relPath = "Files/$path",
        file = file,
        fullPath = file.absolutePath,
        md5 = "NOT_CALCULATED",
        size = -1,
        attributes = attributes.toExtAttributes(FILES_RESERVED_ATTRS)
    )
}
