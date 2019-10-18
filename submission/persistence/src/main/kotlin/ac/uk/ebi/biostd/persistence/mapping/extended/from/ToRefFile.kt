package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.model.ReferencedFile
import ac.uk.ebi.biostd.persistence.model.ReferencedFileAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile

internal fun ExtFile.toRefFile(order: Int) =
    ReferencedFile(
        fileName,
        order,
        file.length(),
        attributes.mapIndexedTo(sortedSetOf(), ::asRefFileAttribute))

private fun asRefFileAttribute(index: Int, attr: ExtAttribute) = ReferencedFileAttribute(attr.toDbAttribute(index))
