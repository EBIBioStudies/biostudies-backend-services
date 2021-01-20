package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.io.sources.FilesSource

internal fun DocFile.toExtFile(filesSource: FilesSource): ExtFile =
    ExtFile(filePath, filesSource.getFile(filePath), attributes.map { it.toExtAttribute() }).also { it.md5 = md5 }

internal fun DocFileTable.toExtFileTable(
    filesSource: FilesSource
): ExtFileTable = ExtFileTable(files.map { it.toExtFile(filesSource) })

internal fun Either<DocFile, DocFileTable>.toExtFiles(
    filesSource: FilesSource
): Either<ExtFile, ExtFileTable> = bimap({ it.toExtFile(filesSource) }) { it.toExtFileTable(filesSource) }

internal fun DocFileList.toExtFileList(
    filesSource: FilesSource
) = ExtFileList(fileName, files.map { it.toExtFile(filesSource) })
