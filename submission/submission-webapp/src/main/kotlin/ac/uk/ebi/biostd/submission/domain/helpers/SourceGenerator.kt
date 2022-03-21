package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.submission.model.GroupSource
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesListSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import java.io.File
import java.nio.file.Path

class SourceGenerator(
    private val fireWebClient: FireWebClient
) {
    fun userSources(
        user: SecurityUser,
        rootPath: String? = null
    ): FilesSource = ComposedFileSource(userSourcesList(user, rootPath.orEmpty()))

    fun submissionSources(requestSources: RequestSources): FilesSource {
        val (owner, submitter, files, rootPath, previousFiles) = requestSources
        return ComposedFileSource(submissionSources(owner, submitter, files, rootPath.orEmpty(), previousFiles))
    }

    private fun submissionSources(
        owner: SecurityUser?,
        submitter: SecurityUser?,
        files: List<File>,
        rootPath: String,
        previousFiles: List<ExtFile>
    ): List<FilesSource> {
        val sources = mutableListOf<FilesSource>(FilesListSource(files))

        submitter?.let {
            sources.add(createPathSource(it.magicFolder.path, rootPath))
            sources.addAll(groupSources(it.groupsFolders))
        }
        owner?.let {
            sources.add(createPathSource(it.magicFolder.path, rootPath))
            sources.addAll(groupSources(it.groupsFolders))
        }

        sources.add(submissionsList(previousFiles))

        return sources
    }

    private fun userSourcesList(user: SecurityUser, rootPath: String): List<FilesSource> =
        listOf(createPathSource(user.magicFolder.path, rootPath)).plus(groupSources(user.groupsFolders))

    private fun submissionsList(listFiles: List<ExtFile>): FilesSource = ExtFileListSource(fireWebClient, listFiles)

    private fun createPathSource(folder: Path, rootPath: String) = PathFilesSource(folder.resolve(rootPath))

    private fun groupSources(groups: List<GroupMagicFolder>) = groups.map { GroupSource(it.groupName, it.path) }
}

data class RequestSources(
    val owner: SecurityUser? = null,
    val submitter: SecurityUser? = null,
    val files: List<File> = emptyList(),
    val rootPath: String? = null,
    val previousFiles: List<ExtFile> = emptyList(),
)
