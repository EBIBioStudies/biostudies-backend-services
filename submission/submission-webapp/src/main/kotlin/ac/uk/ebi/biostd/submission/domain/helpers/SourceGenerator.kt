package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.submission.model.GroupSource
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.ListFilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

class SourceGenerator {
    fun getSubmissionSources(user: SecurityUser, files: List<File>, rootPath: String): FilesSource {
        val sources = mutableListOf<FilesSource>()
        sources.add(ListFilesSource(files))
        sources.add(userFiles(user, rootPath))
        sources.addAll(user.groupsFolders.map { GroupSource(it.groupName, it.path) })
        return ComposedFileSource(sources)
    }

    fun getSubmissionSources(user: SecurityUser, rootPath: String): FilesSource {
        val sources = mutableListOf<FilesSource>()
        sources.add(userFiles(user, rootPath))
        sources.addAll(user.groupsFolders.map { GroupSource(it.groupName, it.path) })
        return ComposedFileSource(sources)
    }

    private fun userFiles(user: SecurityUser, rootPath: String) =
        PathFilesSource(user.magicFolder.path.resolve(rootPath))
}
