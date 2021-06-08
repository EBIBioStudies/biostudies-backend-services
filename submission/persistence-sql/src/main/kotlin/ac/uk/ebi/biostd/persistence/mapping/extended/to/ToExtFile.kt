package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ac.uk.ebi.biostd.persistence.model.ext.validAttributes
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.sources.FilesSource

internal fun DbFile.toExtFile(fileSource: FilesSource) =
    NfsFile(name, fileSource.getFile(name), validAttributes.map { it.toExtAttribute() })

internal fun DbReferencedFile.toExtFile(fileSource: FilesSource) =
    NfsFile(name, fileSource.getFile(name), validAttributes.map { it.toExtAttribute() })
