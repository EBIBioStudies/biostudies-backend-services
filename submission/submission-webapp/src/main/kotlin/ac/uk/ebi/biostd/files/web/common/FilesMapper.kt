package ac.uk.ebi.biostd.files.web.common

import ac.uk.ebi.biostd.files.model.FilesSpec
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.UserFileType.Companion.getType
import java.io.File
import java.nio.file.Path

private const val USER_FOLDER_NAME = "User"
private const val GROUP_FOLDER_NAME = "Groups"

class FilesMapper {

    fun asUserFiles(fileSpec: FilesSpec): List<UserFile> =
        fileSpec.files.map { asUserFile(it, fileSpec, USER_FOLDER_NAME) }

    fun asGroupFiles(fileSpec: FilesSpec): List<UserFile> =
        fileSpec.files.map { asUserFile(it, fileSpec, GROUP_FOLDER_NAME) }

    private fun asUserFile(file: File, fileSpec: FilesSpec, prefix: String) =
        UserFile(file.name, getPath(file, fileSpec.source, prefix), file.length(), getType(file))

    private fun getPath(file: File, source: Path, replacement: String) =
        file.absolutePath.replace(source.toAbsolutePath().toString(), replacement)
}
