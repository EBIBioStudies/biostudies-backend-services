package ebi.ac.uk.api

import java.io.File

class UserFile(
    val name: String,
    val path: String,
    val size: Long,
    val type: UserFileType
)

enum class UserFileType {
    FILE,
    DIR;

    companion object {
        fun getType(file: File) = if (file.isDirectory) UserFileType.DIR else UserFileType.FILE
    }
}
