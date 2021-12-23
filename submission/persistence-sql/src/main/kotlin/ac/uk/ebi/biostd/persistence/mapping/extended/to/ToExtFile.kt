package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ac.uk.ebi.biostd.persistence.model.ext.validAttributes
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.FireBioFile
import ebi.ac.uk.io.sources.FireDirectoryBioFile
import ebi.ac.uk.io.sources.NfsBioFile

internal fun DbFile.toExtFile(fileSource: FilesSource): NfsFile {
    return when (val bioFile = fileSource.getFile(name)) {
        is FireBioFile -> TODO()
        is FireDirectoryBioFile -> TODO()
        is NfsBioFile -> createNfsFile(
            name,
            "Files/$name",
            bioFile.file,
            validAttributes.map { it.toExtAttribute() }
        )
    }
}

internal fun DbReferencedFile.toExtFile(fileSource: FilesSource): NfsFile {
    return when (val bioFile = fileSource.getFile(name)) {
        is FireBioFile -> TODO()
        is FireDirectoryBioFile -> TODO()
        is NfsBioFile -> createNfsFile(
            name,
            "Files/$name",
            bioFile.file,
            validAttributes.map { it.toExtAttribute() }
        )
    }
}
