package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.mapping.from.toExtAttributes
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.constants.FILES_RESERVED_ATTRS
import java.io.File

internal fun create(path: String, file: File, calculateProperties: Boolean, attributes: List<Attribute>): NfsFile {
    return NfsFile(
        filePath = path,
        relPath = "Files/$path",
        file = file,
        fullPath = file.absolutePath,
        md5 = if (calculateProperties) file.md5() else "NOT_CALCULATED",
        size = if (calculateProperties) file.size() else -1,
        attributes = attributes.toExtAttributes(FILES_RESERVED_ATTRS)
    )
}
