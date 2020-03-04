package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FilesSource

internal fun DbFile.toExtFile(fileSource: FilesSource) =
    ExtFile(name, md5, fileSource.getFile(name), attributes.map { it.toExtAttribute() })

internal fun DbReferencedFile.toExtFile(fileSource: FilesSource) =
    ExtFile(name, md5, fileSource.getFile(name), attributes.map { it.toExtAttribute() })
