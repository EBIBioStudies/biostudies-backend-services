package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ac.uk.ebi.biostd.persistence.model.DbReferencedFileAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile

internal fun ExtFile.toRefFile(order: Int) =
    DbReferencedFile(
        fileName,
        order,
        file.length(),
        attributes.mapIndexedTo(sortedSetOf(), ::asRefFileAttribute))

private fun asRefFileAttribute(index: Int, attr: ExtAttribute) = DbReferencedFileAttribute(attr.toDbAttribute(index))
