package ac.uk.ebi.biostd.persistence.mapping.db.extensions

import ac.uk.ebi.biostd.persistence.model.ReferencedFile
import ac.uk.ebi.biostd.persistence.model.ReferencedFileAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile

internal fun ExtFile.toRefFile() =
    ReferencedFile(
        fileName,
        file.length(),
        attributes.mapIndexedTo(sortedSetOf(), ::asReferenceFileAttribute))

private fun asReferenceFileAttribute(index: Int, attr: ExtAttribute) = ReferencedFileAttribute(attr.toDbAttribute(index))

