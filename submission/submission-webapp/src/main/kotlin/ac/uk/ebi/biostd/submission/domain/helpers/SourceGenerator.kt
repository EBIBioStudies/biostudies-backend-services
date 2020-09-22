package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.persistence.mapping.extended.to.USER_PREFIX
import ac.uk.ebi.biostd.submission.model.GroupSource
import com.google.common.collect.ImmutableList
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.ListFilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File
import java.nio.file.Path

class SourceGenerator {
    fun submissionSources(requestSources: RequestSources): FilesSource {
        val (user, files, rootPath, subFolder) = requestSources
        val sources = ImmutableList.builder<FilesSource>().add(ListFilesSource(files))
        user?.let {
            sources.add(createPathSource(user.magicFolder.path, rootPath.orEmpty()))
            sources.addAll(groupSources(user.groupsFolders))
        }

        return when (subFolder) {
            null -> ComposedFileSource(sources.build())
            else -> ComposedFileSource(sources.addAll(submissionsPaths(subFolder)).build())
        }
    }

    private fun submissionsPaths(subFolder: File) =
        listOf(PathFilesSource(subFolder.toPath()), PathFilesSource(subFolder.resolve(USER_PREFIX).toPath()))

    private fun createPathSource(folder: Path, rootPath: String?) = PathFilesSource(folder.resolve(rootPath.orEmpty()))

    private fun groupSources(groups: List<GroupMagicFolder>) = groups.map { GroupSource(it.groupName, it.path) }
}

data class RequestSources(
    val user: SecurityUser? = null,
    val files: List<File> = emptyList(),
    val rootPath: String? = null,
    val subFolder: File? = null
)
