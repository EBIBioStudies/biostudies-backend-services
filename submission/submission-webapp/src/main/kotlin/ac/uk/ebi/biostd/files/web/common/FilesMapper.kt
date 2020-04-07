package ac.uk.ebi.biostd.files.web.common

import ac.uk.ebi.biostd.files.model.FilesSpec
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.UserFileType.Companion.getType
import ebi.ac.uk.io.size
import java.io.File
import java.nio.file.Path

private const val USER_FOLDER_NAME = "user"
private const val GROUP_FOLDER_NAME = "groups"

class FilesMapper {

    fun asUserFiles(fileSpec: FilesSpec): List<UserFile> =
        fileSpec.files.map { asUserFile(it, fileSpec, USER_FOLDER_NAME) }

    fun asGroupFiles(groupName: String, fileSpec: FilesSpec): List<UserFile> =
        fileSpec.files.map { asUserFile(it, fileSpec, "$GROUP_FOLDER_NAME/$groupName") }

    private fun asUserFile(file: File, fileSpec: FilesSpec, prefix: String) =
        UserFile(file.name, getPath(file, fileSpec.source, prefix), file.size(), getType(file))

    private fun getPath(file: File, source: Path, replacement: String) =
        file.parentFile.absolutePath.replace(source.toAbsolutePath().toString(), replacement)
}
