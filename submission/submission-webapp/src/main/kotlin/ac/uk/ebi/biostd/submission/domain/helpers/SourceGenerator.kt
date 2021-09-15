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

class SourceGenerator(private val fireWebClient: FireWebClient) {

    fun submissionSources(requestSources: RequestSources): FilesSource {
        val (user, files, rootPath, previousFiles) = requestSources
        return ComposedFileSource(submissionSources(user, files, rootPath.orEmpty(), previousFiles))
    }

    private fun submissionSources(
        user: SecurityUser?,
        files: List<File>,
        rootPath: String,
        previousFiles: List<ExtFile>
    ): List<FilesSource> {
        val sources = mutableListOf<FilesSource>(FilesListSource(files))

        user?.let {
            sources.add(createPathSource(user.magicFolder.path, rootPath))
            sources.addAll(groupSources(user.groupsFolders))
        }

        sources.add(submissionsList(previousFiles))

        return sources
    }

    private fun submissionsList(listFiles: List<ExtFile>): FilesSource = ExtFileListSource(fireWebClient, listFiles)
    private fun createPathSource(folder: Path, rootPath: String) = PathFilesSource(folder.resolve(rootPath))
    private fun groupSources(groups: List<GroupMagicFolder>) = groups.map { GroupSource(it.groupName, it.path) }
}

data class RequestSources(
    val user: SecurityUser? = null,
    val files: List<File> = emptyList(),
    val rootPath: String? = null,
    val previousFiles: List<ExtFile> = emptyList()
)
