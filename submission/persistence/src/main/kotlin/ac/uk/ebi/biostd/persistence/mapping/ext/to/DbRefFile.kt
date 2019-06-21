package ac.uk.ebi.biostd.persistence.mapping.ext.to

import ac.uk.ebi.biostd.persistence.model.ReferencedFile
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.utils.FilesSource

internal fun ReferencedFile.toExtRefFile(filesSource: FilesSource): ExtFile =
    ExtFile(name, filesSource.getFile(name), attributes.map { it.toExtAttribute() })

