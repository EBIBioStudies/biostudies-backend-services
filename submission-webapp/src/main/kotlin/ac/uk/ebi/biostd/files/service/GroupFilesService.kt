package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.exception.UserGroupNotFound
import ac.uk.ebi.biostd.files.model.FilesSpec
import ac.uk.ebi.biostd.files.utils.copyFile
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ebi.ac.uk.io.asFileList
import ebi.ac.uk.model.User
import ebi.ac.uk.paths.FolderResolver
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path

class GroupFilesService(
    private val folderResolver: FolderResolver,
    private val userGroupRepository: UserGroupDataRepository
) {
    fun listFiles(groupName: String, user: User, path: String): FilesSpec {
        val groupPath = getGroupPath(groupName, user)
        val file = groupPath.resolve(path).toFile()
        return FilesSpec(groupPath, file.asFileList())
    }

    fun uploadFiles(groupName: String, user: User, path: String, files: Array<MultipartFile>) {
        val groupPath = getGroupPath(groupName, user)
        files.forEach { file -> copyFile(groupPath.resolve(path), file) }
    }

    fun createFolder(groupName: String, user: User, path: String, folderName: String) {
        val groupPath = getGroupPath(groupName, user)
        val folder = groupPath
            .resolve(path)
            .resolve(folderName)
        folder.toFile().mkdirs()
    }

    fun deleteFile(groupName: String, user: User, path: String, fileName: String) {
        val groupPath = getGroupPath(groupName, user)
        val file = groupPath
            .resolve(path)
            .resolve(fileName)
            .toFile()
        file.deleteRecursively()
    }

    private fun getGroupPath(groupName: String, user: User): Path {
        val group = userGroupRepository.findByNameAndUsersId(groupName, user.id)
            .orElseThrow { UserGroupNotFound(user, groupName) }
        return folderResolver.getGroupMagicFolderPath(group.id, group.secret)
    }
}
