package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.model.GroupSource
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtFileOrigin.SUBMISSION
import ebi.ac.uk.extended.model.ExtFileOrigin.USER_SPACE
import ebi.ac.uk.extended.model.ExtFileOrigin as PreferredFilesSource
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesListSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File
import java.nio.file.Paths

class SourceGenerator(
    private val props: ApplicationProperties,
    private val fireSourceFactory: FireFilesSourceFactory,
) {
    fun userSources(
        user: SecurityUser,
        rootPath: String? = null
    ): FilesSource = ComposedFileSource(userSourcesList(user, rootPath.orEmpty()), USER_SPACE)

    fun submissionSources(requestSources: RequestSources): FilesSource {
        val (owner, submitter, files, rootPath, submission, preferredSource) = requestSources

        val submissionSources = buildList {
            add(FilesListSource(files, USER_SPACE))

            addUserSources(submitter, owner, rootPath)
            addSubmissionSources(submission)
        }

        return ComposedFileSource(submissionSources, preferredSource)
    }

    private fun MutableList<FilesSource>.addUserSources(
        submitter: SecurityUser?,
        owner: SecurityUser?,
        rootPath: String?
    ) {
        if (submitter != null) {
            add(userSource(submitter, rootPath))
            addAll(groupSources(submitter.groupsFolders))
        }

        if (owner != null) {
            add(userSource(owner, rootPath))
            addAll(groupSources(owner.groupsFolders))
        }
    }

    private fun MutableList<FilesSource>.addSubmissionSources(sub: ExtSubmission?) {
        if (props.persistence.enableFire) {
            if (sub == null) add(fireSourceFactory.createFireSource())
            else add(fireSourceFactory.createSubmissionFireSource(sub.accNo, Paths.get("${sub.relPath}/Files")))
        }

        if (props.persistence.enableFire.not() && sub != null) {
            add(PathFilesSource(Paths.get(props.submissionPath).resolve(sub.relPath).resolve(FILES_PATH), SUBMISSION))
        }
    }

    private fun userSourcesList(user: SecurityUser, rootPath: String): List<FilesSource> =
        listOf(userSource(user, rootPath)).plus(groupSources(user.groupsFolders))

    private fun userSource(user: SecurityUser, rootPath: String?): PathFilesSource {
        val folder = user.magicFolder.path.resolve(rootPath.orEmpty())
        return PathFilesSource(folder, USER_SPACE)
    }

    private fun groupSources(groups: List<GroupMagicFolder>): List<GroupSource> =
        groups.map { GroupSource(it.groupName, it.path) }
}

data class RequestSources(
    val owner: SecurityUser? = null,
    val submitter: SecurityUser? = null,
    val files: List<File> = emptyList(),
    val rootPath: String?,
    val submission: ExtSubmission?,
    val preferredSource: PreferredFilesSource = USER_SPACE
)
