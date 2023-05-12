package ac.uk.ebi.biostd.files.web.common

import ac.uk.ebi.biostd.files.model.FilesSpec
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.api.UserFileType.Companion.getType
import ebi.ac.uk.io.ext.size
import java.io.File

private const val USER_FOLDER_NAME = "user"
private const val GROUP_FOLDER_NAME = "groups"

class FilesMapper {
    fun asUserFiles(fileSpec: FilesSpec): List<UserFile> {
        return fileSpec.files.map { asUserFile(it, fileSpec, USER_FOLDER_NAME) }
    }

    fun asGroupFiles(groupName: String, fileSpec: FilesSpec): List<UserFile> {
        return fileSpec.files.map { asUserFile(it, fileSpec, "$GROUP_FOLDER_NAME/$groupName") }
    }

    private fun asUserFile(file: File, fileSpec: FilesSpec, prefix: String): UserFile {
        val source = fileSpec.source.toAbsolutePath().toString()
        val path = file.parentFile.absolutePath.replace(source, prefix)
        val size = file.size(calculateDirectories = false)

        return UserFile(file.name, path, size, getType(file))
    }
}
