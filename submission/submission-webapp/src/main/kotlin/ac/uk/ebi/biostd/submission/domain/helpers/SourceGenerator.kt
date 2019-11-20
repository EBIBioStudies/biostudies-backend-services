package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.submission.model.GroupSource
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.ListFilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

@Suppress("SpreadOperator")
class SourceGenerator {
    fun getSubmissionSources(user: SecurityUser, files: List<File>, rootPath: String) =
        ComposedFileSource(userFiles(user, rootPath), requestFiles(files), *getGroupSources(user.groupsFolders))

    fun getSubmissionSources(user: SecurityUser, rootPath: String) =
        ComposedFileSource(userFiles(user, rootPath), *getGroupSources(user.groupsFolders))

    private fun userFiles(user: SecurityUser, rootPath: String) =
        PathFilesSource(user.magicFolder.path.resolve(rootPath))

    private fun requestFiles(files: List<File>) = ListFilesSource(files)

    private fun getGroupSources(groups: List<GroupMagicFolder>): Array<FilesSource> =
        groups.map { GroupSource(it.groupName, it.path) }.toTypedArray()
}
