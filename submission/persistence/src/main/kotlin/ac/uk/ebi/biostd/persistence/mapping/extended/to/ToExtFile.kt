package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.File
import ac.uk.ebi.biostd.persistence.model.ReferencedFile
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FilesSource

internal fun File.toExtFile(fileSource: FilesSource) =
    ExtFile(name, fileSource.getFile(name), attributes.map { it.toExtAttribute() })

internal fun ReferencedFile.toExtFile(fileSource: FilesSource) =
    ExtFile(name, fileSource.getFile(name), attributes.map { it.toExtAttribute() })
