package ac.uk.ebi.biostd.persistence.mapping.ext.extensions

import ac.uk.ebi.biostd.persistence.model.File
import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.extended.model.ExtFile

internal fun File.toExtFile(fileSource: FilesSource) =
    ExtFile(name, fileSource.get(name), attributes.map { it.toExtAttribute() })

