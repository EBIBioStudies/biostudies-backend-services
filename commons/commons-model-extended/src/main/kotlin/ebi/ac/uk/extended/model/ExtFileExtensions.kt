package ebi.ac.uk.extended.model

import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import java.io.File

val ExtFile.storageMode: StorageMode
    get() = when (this) {
        is NfsFile -> NFS
        is FireFile -> FIRE
    }

fun ExtFile.copyWithAttributes(attributes: List<ExtAttribute>): ExtFile {
    return when (this) {
        is FireFile -> this.copy(attributes = attributes)
        is NfsFile -> this.copy(attributes = attributes)
    }
}

fun ExtFile.asFireFile(fireId: String, firePath: String?, published: Boolean): FireFile = FireFile(
    fireId,
    firePath,
    published,
    filePath,
    relPath,
    md5,
    size,
    type,
    attributes
)

fun ExtFile.asNfsFile(file: File): NfsFile = NfsFile(
    filePath,
    relPath,
    file,
    file.absolutePath,
    md5,
    size,
    attributes,
    type
)
