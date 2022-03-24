package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.submission.model.GroupSource
import ebi.ac.uk.base.nullIfBlank
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesListSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File
import java.nio.file.Path

class SourceGenerator {
    fun userSources(
        user: SecurityUser,
        rootPath: String? = null
    ): FilesSource = ComposedFileSource(userSourcesList(user, rootPath.orEmpty()))

    fun submissionSources(requestSources: RequestSources): FilesSource {
        val (owner, submitter, files, rootPath, submissionPath) = requestSources
        return ComposedFileSource(submissionSources(owner, submitter, files, rootPath.nullIfBlank(), submissionPath))
    }

    private fun submissionSources(
        owner: SecurityUser?,
        submitter: SecurityUser?,
        files: List<File>,
        rootPath: String?,
        submissionPath: Path?
    ): List<FilesSource> = buildList {
        add(FilesListSource(files))
        submitter?.let {
            add(createPathSource(it, rootPath))
            addAll(groupSources(it.groupsFolders))
        }
        owner?.let {
            add(createPathSource(it, rootPath))
            addAll(groupSources(it.groupsFolders))
        }
        submissionPath?.let {
            add(PathFilesSource(submissionPath.resolve(FILES_PATH)))
        }
    }

    private fun userSourcesList(user: SecurityUser, rootPath: String): List<FilesSource> =
        listOf(createPathSource(user, rootPath)).plus(groupSources(user.groupsFolders))

    private fun createPathSource(user: SecurityUser, rootPath: String?): PathFilesSource {
        val folder = user.magicFolder.path.resolve(rootPath.orEmpty())
        return PathFilesSource(folder)
    }

    private fun groupSources(groups: List<GroupMagicFolder>): List<GroupSource> =
        groups.map { GroupSource(it.groupName, it.path) }
}

data class RequestSources(
    val owner: SecurityUser? = null,
    val submitter: SecurityUser? = null,
    val files: List<File> = emptyList(),
    val rootPath: String? = null,
    val submissionPath: Path? = null,
    val subRelPath: Path? = null
)
