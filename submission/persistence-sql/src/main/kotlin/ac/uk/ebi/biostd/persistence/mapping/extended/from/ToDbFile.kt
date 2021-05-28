package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbFileAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils

internal fun ExtFile.toDbFile(order: Int, tableIndex: Int = NO_TABLE_INDEX) = when(this) {
    is FireFile -> TODO()
    is NfsFile -> DbFile(
        fileName,
        order,
        FileUtils.size(file),
        attributes.mapIndexedTo(sortedSetOf(), ::toDbFileAttribute),
        FileUtils.isDirectory(file),
        tableIndex
    )
}

private fun toDbFileAttribute(index: Int, attr: ExtAttribute) = DbFileAttribute(attr.toDbAttribute(index))
