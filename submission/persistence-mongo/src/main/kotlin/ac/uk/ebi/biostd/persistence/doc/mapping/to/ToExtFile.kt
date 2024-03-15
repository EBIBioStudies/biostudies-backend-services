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

internal fun DocFile.toExtFile(subRelPath: String): ExtFile = when (this) {
    is FireDocFile -> FireFile(
        fireId = fireId,
        firePath = "$subRelPath/$relPath",
        published = published,
        filePath = filePath,
        relPath = relPath,
        md5 = md5,
        size = fileSize,
        type = ExtFileType.fromString(fileType),
        attributes = attributes.toExtAttributes()
    )

    is NfsDocFile -> NfsFile(
        filePath = filePath,
        relPath = relPath,
        file = Paths.get(fullPath).toFile(),
        fullPath = fullPath,
        md5 = md5,
        size = fileSize,
        attributes = attributes.toExtAttributes()
    )
}

internal fun DocFileTable.toExtFileTable(subRelPath: String): ExtFileTable {
    return ExtFileTable(files.map { it.toExtFile(subRelPath) })
}

internal fun Either<DocFile, DocFileTable>.toExtFiles(
    subRelPath: String,
): Either<ExtFile, ExtFileTable> {
    return bimap({ it.toExtFile(subRelPath) }) { it.toExtFileTable(subRelPath) }
}
