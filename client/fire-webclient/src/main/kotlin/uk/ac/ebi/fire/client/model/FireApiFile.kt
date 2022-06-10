package uk.ac.ebi.fire.client.model

import uk.ac.ebi.fire.client.api.FIRE_BIO_ACC_NO

data class FireApiFile(
    val objectId: Number,
    val fireOid: String,
    val objectMd5: String,
    val objectSize: Number,
    val createTime: String,
    val metadata: List<MetadataEntry>? = null,
    val filesystemEntry: FileSystemEntry? = null,
)

fun FireApiFile.hasNoPath(): Boolean = filesystemEntry?.path == null

fun FireApiFile.isAvailable(accNo: String): Boolean {
    val meta = metadata.orEmpty()
    return meta.none { it.key == FIRE_BIO_ACC_NO } || meta.contains(MetadataEntry(FIRE_BIO_ACC_NO, accNo))
}

fun FireApiFile.isAvailable(): Boolean = metadata.orEmpty().none { it.key == FIRE_BIO_ACC_NO }

data class FileSystemEntry(
    val path: String,
    val published: Boolean,
)

data class MetadataEntry(
    val key: String,
    val value: String,
) {
    override fun toString(): String = "\"${key}\": \"${value}\""
}
