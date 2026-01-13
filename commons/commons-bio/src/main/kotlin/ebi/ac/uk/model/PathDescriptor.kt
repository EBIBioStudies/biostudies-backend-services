package ebi.ac.uk.model

class UserPath(
    val path: String,
)

class GroupPath(
    val path: String,
)

class FileListPath(
    val path: String,
)

class FilePath(
    val path: String,
)

class DirFilePath(
    val path: String,
    val fileName: String,
)

class RenameFilePath(
    val path: String,
    val originalName: String,
    val newName: String,
)
