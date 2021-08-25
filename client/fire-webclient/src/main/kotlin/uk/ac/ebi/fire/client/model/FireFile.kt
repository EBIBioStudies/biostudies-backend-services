package uk.ac.ebi.fire.client.model

data class FireFile(
    val objectId: Number,
    val fireOid: String,
    val objectMd5: String,
    val objectSize: Number,
    val createTime: String,
    val filesystemEntry: FileSystemEntry? = null
)
