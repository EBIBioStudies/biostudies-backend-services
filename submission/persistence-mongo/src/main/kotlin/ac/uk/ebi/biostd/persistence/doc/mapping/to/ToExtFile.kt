package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import java.nio.file.Paths

internal fun DocFile.toExtFile(): ExtFile =
    ExtFile(relPath, Paths.get(fullPath).toFile(), attributes.map { it.toExtAttribute() }).also { it.md5 = md5 }

internal fun DocFileTable.toExtFileTable(): ExtFileTable = ExtFileTable(files.map { it.toExtFile() })

internal fun Either<DocFile, DocFileTable>.toExtFiles(): Either<ExtFile, ExtFileTable> =
    bimap({ it.toExtFile() }) { it.toExtFileTable() }

/**
 * Maps a DocFileList to corresponding Ext type. Note that empty list is used as files as list files are not loaded as
 * part of the submission.
 */
internal fun DocFileList.toExtFileList() = ExtFileList(fileName, emptyList())
