package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.model.GroupSource
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesListSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class SourceGenerator(
    private val props: ApplicationProperties,
    private val fireSourceFactory: FireFilesSourceFactory,
) {
    fun userSources(
        user: SecurityUser,
        rootPath: String? = null
    ): FilesSource = ComposedFileSource(userSourcesList(user, rootPath.orEmpty()))

    fun submissionSources(requestSources: RequestSources): FilesSource {
        val (owner, submitter, files, submission) = requestSources
        return ComposedFileSource(submissionSources(owner, submitter, files, submission))
    }

    private fun submissionSources(
        owner: SecurityUser?,
        submitter: SecurityUser?,
        files: List<File>,
        submission: ExtSubmission?
    ): List<FilesSource> {
        return buildList {
            add(FilesListSource(files))

            if (submitter != null) {
                add(createPathSource(submitter, submission?.rootPath))
                addAll(groupSources(submitter.groupsFolders))
            }

            if (owner != null) {
                add(createPathSource(owner, submission?.rootPath))
                addAll(groupSources(owner.groupsFolders))
            }

            if (submission == null)
                add(fireSourceFactory.createFireSource())
            else {
                add(PathFilesSource(Paths.get(props.submissionPath).resolve(submission.relPath).resolve(FILES_PATH)))
                add(fireSourceFactory.createSubmissionFireSource(submission.accNo, Paths.get(submission.relPath)))
            }
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
    val submission: ExtSubmission?
)

private data class SubmissionInfo(
    val accNo: String,
    val rootPath: String,
    val submissionPath: Path,
    val relPath: Path
)
