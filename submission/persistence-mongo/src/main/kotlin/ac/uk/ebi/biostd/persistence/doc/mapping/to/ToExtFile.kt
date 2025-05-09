package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.model.RequestDocFile
import ebi.ac.uk.base.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.RequestFile
import java.nio.file.Paths

internal fun DocFile.toExtFile(
    released: Boolean,
    subRelPath: String,
): ExtFile =
    when (this) {
        is FireDocFile ->
            FireFile(
                fireId = fireId,
                firePath = "$subRelPath/$relPath",
                published = released,
                filePath = filePath,
                relPath = relPath,
                md5 = md5,
                size = fileSize,
                type = ExtFileType.fromString(fileType),
                attributes = attributes.toExtAttributes(),
            )

        is NfsDocFile ->
            NfsFile(
                filePath = filePath,
                relPath = relPath,
                file = Paths.get(fullPath).toFile(),
                fullPath = fullPath,
                md5 = md5,
                size = fileSize,
                attributes = attributes.toExtAttributes(),
            )

        is RequestDocFile -> RequestFile(filePath, attributes.toExtAttributes(), type)
    }

internal fun DocFileTable.toExtFileTable(
    released: Boolean,
    subRelPath: String,
): ExtFileTable = ExtFileTable(files.map { it.toExtFile(released, subRelPath) })

internal fun Either<DocFile, DocFileTable>.toExtFiles(
    released: Boolean,
    subRelPath: String,
): Either<ExtFile, ExtFileTable> = bimap({ it.toExtFile(released, subRelPath) }) { it.toExtFileTable(released, subRelPath) }
