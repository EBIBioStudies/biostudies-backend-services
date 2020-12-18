package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ac.uk.ebi.biostd.persistence.model.DbReferencedFileAttribute

val DbReferencedFile.validAttributes: List<DbReferencedFileAttribute>
    get() = attributes.filterNot { it.value.isBlank() }
