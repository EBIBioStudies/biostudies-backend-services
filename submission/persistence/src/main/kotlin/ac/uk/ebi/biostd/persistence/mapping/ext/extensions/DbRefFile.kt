package ac.uk.ebi.biostd.persistence.mapping.ext.extensions

import ac.uk.ebi.biostd.persistence.model.ReferencedFile
import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.extended.model.ExtFile

internal fun ReferencedFile.toExtRefFile(filesSource: FilesSource): ExtFile =
    ExtFile(name, filesSource.get(name), attributes.map { it.toExtAttribute() })


