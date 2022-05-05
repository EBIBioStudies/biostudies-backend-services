package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import java.nio.file.Paths

internal fun DocFile.toExtFile(): ExtFile = when (this) {
    is FireDocFile -> FireFile(
        filePath,
        relPath,
        fireId,
        md5,
        fileSize,
        ExtFileType.fromString(fileType),
        attributes.toExtAttributes()
    )
    is NfsDocFile -> NfsFile(
        filePath,
        relPath,
        Paths.get(fullPath).toFile(),
        fullPath,
        md5,
        fileSize,
        attributes.toExtAttributes()
    )
}

internal fun DocFileTable.toExtFileTable(): ExtFileTable = ExtFileTable(files.map { it.toExtFile() })

internal fun Either<DocFile, DocFileTable>.toExtFiles(): Either<ExtFile, ExtFileTable> =
    bimap({ it.toExtFile() }) { it.toExtFileTable() }
