package ac.uk.ebi.biostd.persistence.mapping.db.extensions

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.File
import ac.uk.ebi.biostd.persistence.model.FileAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile

internal fun ExtFile.toDbFile(order: Int, tableIndex: Int = NO_TABLE_INDEX) =
    File(fileName, order, file.length(), attributes.mapIndexedTo(sortedSetOf(), ::asFileAttribute), tableIndex)

private fun asFileAttribute(index: Int, attr: ExtAttribute) = FileAttribute(attr.toDbAttribute(index))

