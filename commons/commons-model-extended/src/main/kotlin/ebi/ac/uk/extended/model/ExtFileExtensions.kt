package ebi.ac.uk.extended.model

import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import java.io.File

val ExtFile.storageMode: StorageMode
    get() =
        when (this) {
            is NfsFile -> NFS
            is FireFile -> FIRE
            is RequestFile -> error("RequestFile does not have an storage mode")
        }

fun ExtFile.copyWithAttributes(attributes: List<ExtAttribute>): ExtFile =
    when (this) {
        is FireFile -> copy(attributes = attributes)
        is NfsFile -> copy(attributes = attributes)
        is RequestFile -> copy(attributes = attributes)
    }

fun NfsFile.asFireFile(
    fireId: String,
    firePath: String,
    published: Boolean,
): FireFile =
    FireFile(
        fireId = fireId,
        firePath = firePath,
        published = published,
        filePath = filePath,
        relPath = relPath,
        md5 = md5,
        size = size,
        type = type,
        attributes = attributes,
    )

fun FireFile.asNfsFile(file: File): NfsFile =
    NfsFile(
        filePath = filePath,
        relPath = relPath,
        file = file,
        fullPath = file.absolutePath,
        md5Calculated = true,
        md5 = md5,
        size = size,
        attributes = attributes,
        type = type,
    )
