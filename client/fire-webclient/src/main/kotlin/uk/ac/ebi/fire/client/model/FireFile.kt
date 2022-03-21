package uk.ac.ebi.fire.client.model

data class FireFile(
    val objectId: Number,
    val fireOid: String,
    val objectMd5: String,
    val objectSize: Number,
    val createTime: String,
    val metadata: List<MetadataEntry>? = null,
    val filesystemEntry: FileSystemEntry? = null
)

data class FileSystemEntry(
    val path: String,
    val published: Boolean
)

data class MetadataEntry(
    val key: String,
    val value: String
) {
    override fun toString(): String = "\"${key}\": \"${value}\""
}
