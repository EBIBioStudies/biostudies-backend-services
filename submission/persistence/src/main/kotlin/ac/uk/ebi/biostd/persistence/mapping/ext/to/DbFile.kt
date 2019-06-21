package ac.uk.ebi.biostd.persistence.mapping.ext.to

import ac.uk.ebi.biostd.persistence.model.File
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.utils.FilesSource

internal fun File.toExtFile(fileSource: FilesSource) =
    ExtFile(name, fileSource.getFile(name), attributes.map { it.toExtAttribute() })
