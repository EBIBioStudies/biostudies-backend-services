package uk.ac.ebi.io.builder

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.size
import java.io.File

internal fun createFile(
    path: String,
    file: File,
    size: Long,
    type: ExtFileType,
    attributes: List<ExtAttribute>,
): NfsFile =
    NfsFile(
        filePath = path,
        relPath = "Files/$path",
        file = file,
        fullPath = file.absolutePath,
        md5 = "NOT_CALCULATED",
        size = size,
        type = type,
        attributes = attributes,
    )

internal fun createFile(
    path: String,
    file: File,
    attributes: List<ExtAttribute>,
): NfsFile =
    NfsFile(
        filePath = path,
        relPath = "Files/$path",
        file = file,
        fullPath = file.absolutePath,
        md5 = "NOT_CALCULATED",
        size = file.size(calculateDirectories = false),
        attributes = attributes,
    )
