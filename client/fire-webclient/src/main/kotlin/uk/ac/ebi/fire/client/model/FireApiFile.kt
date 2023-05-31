package uk.ac.ebi.fire.client.model

import ebi.ac.uk.base.orFalse

data class FireApiFile(
    val objectId: Number,
    val fireOid: String,
    val objectMd5: String,
    val objectSize: Long,
    val createTime: String,
    val filesystemEntry: FileSystemEntry? = null,
) {
    val path: String? = filesystemEntry?.path?.removePrefix("/")
    val published: Boolean = filesystemEntry?.published.orFalse()
}

enum class FileType(val key: String) {
    FILE("file"),
    DIR("directory")
}

data class FileSystemEntry(
    val path: String?,
    val published: Boolean,
)
