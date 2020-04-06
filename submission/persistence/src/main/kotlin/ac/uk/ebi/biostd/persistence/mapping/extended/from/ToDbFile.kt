package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbFileAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import java.io.File
import java.nio.file.Files

internal fun ExtFile.toDbFile(order: Int, tableIndex: Int = NO_TABLE_INDEX) =
    DbFile(fileName, order, fileSize(file), attributes.mapIndexedTo(sortedSetOf(), ::toDbFileAttribute), tableIndex)

private fun toDbFileAttribute(index: Int, attr: ExtAttribute) = DbFileAttribute(attr.toDbAttribute(index))
private fun fileSize(file: File) = Files.size(file.toPath())
