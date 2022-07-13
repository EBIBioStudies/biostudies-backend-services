package uk.ac.ebi.fire.client.model

data class FireApiFile(
    val objectId: Number,
    val fireOid: String,
    val objectMd5: String,
    val objectSize: Number,
    val createTime: String,
    val filesystemEntry: FileSystemEntry? = null,
)

val FireApiFile.path: String?
    get() = filesystemEntry?.path

enum class FileType(val key: String) {
    FILE("file"),
    DIR("directory")
}

data class FileSystemEntry(
    val path: String?,
    val published: Boolean,
)

data class MetadataEntry(
    val key: String,
    val value: String,
) {
    override fun toString(): String = "\"${key}\": \"${value}\""
}
