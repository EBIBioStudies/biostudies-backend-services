package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.ReferencedFileList
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.io.sources.FilesSource

internal const val TO_EXT_FILE_LIST_EXTENSION = "ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtFileListKt"

fun ReferencedFileList.toExtFileList(fileSource: FilesSource): ExtFileList =
    ExtFileList(name, fileSource.getFile(name), files.map { it.toExtFile(fileSource) })
