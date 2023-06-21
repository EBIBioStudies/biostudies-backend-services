package ac.uk.ebi.biostd.files.web.common

import ac.uk.ebi.biostd.files.model.FilesSpec
import ebi.ac.uk.api.UserFile
import kotlin.io.path.Path
import ac.uk.ebi.biostd.files.model.UserFile as UserFolderFile

private const val USER_FOLDER_NAME = "user"
private const val GROUP_FOLDER_NAME = "groups"

class FilesMapper {
    fun asUserFiles(fileSpec: FilesSpec): List<UserFile> {
        return fileSpec.files.map { asUserFile(it, USER_FOLDER_NAME) }
    }

    fun asGroupFiles(groupName: String, fileSpec: FilesSpec): List<UserFile> {
        return fileSpec.files.map { asUserFile(it, "$GROUP_FOLDER_NAME/$groupName") }
    }

    private fun asUserFile(file: UserFolderFile, prefix: String): UserFile {
        return UserFile(file.name, Path(prefix).resolve(file.path).toString(), file.fileSize, file.type)
    }
}
