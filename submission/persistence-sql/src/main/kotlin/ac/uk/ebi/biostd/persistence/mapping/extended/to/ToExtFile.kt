package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ac.uk.ebi.biostd.persistence.model.ext.validAttributes
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FilesSource

internal fun DbFile.toExtFile(fileSource: FilesSource) =
    ExtFile(name, fileSource.getFile(name), validAttributes.map { it.toExtAttribute() })

internal fun DbReferencedFile.toExtFile(fileSource: FilesSource) =
    ExtFile(name, fileSource.getFile(name), validAttributes.map { it.toExtAttribute() })
