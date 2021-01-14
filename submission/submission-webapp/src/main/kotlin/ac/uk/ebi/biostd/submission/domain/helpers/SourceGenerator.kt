package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.persistence.mapping.extended.to.USER_PREFIX
import ac.uk.ebi.biostd.submission.model.GroupSource
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.ListFilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File
import java.nio.file.Path

class SourceGenerator {
    fun submissionSources(requestSources: RequestSources): FilesSource {
        val (user, files, rootPath, subFolder) = requestSources
        return ComposedFileSource(submissionSources(user, files, rootPath.orEmpty(), subFolder))
    }

    private fun submissionSources(
        user: SecurityUser?,
        files: List<File>,
        rootPath: String,
        subFolder: File?
    ): List<FilesSource> {
        val sources = mutableListOf<FilesSource>(ListFilesSource(files))

       user?.let {
           sources.add(createPathSource(user.magicFolder.path, rootPath))
           sources.addAll(groupSources(user.groupsFolders))
       }

       subFolder?.let {
           sources.addAll(submissionsPaths(subFolder))
       }

       return sources
    }

    private fun submissionsPaths(subFolder: File) = listOf(
        PathFilesSource(subFolder.toPath()),
        PathFilesSource(subFolder.resolve(FILES_PATH).toPath()),
        PathFilesSource(subFolder.resolve(FILES_PATH).resolve(USER_PREFIX).toPath())
    )

    private fun createPathSource(folder: Path, rootPath: String) = PathFilesSource(folder.resolve(rootPath))

    private fun groupSources(groups: List<GroupMagicFolder>) = groups.map { GroupSource(it.groupName, it.path) }
}

data class RequestSources(
    val user: SecurityUser? = null,
    val files: List<File> = emptyList(),
    val rootPath: String? = null,
    val subFolder: File? = null
)
