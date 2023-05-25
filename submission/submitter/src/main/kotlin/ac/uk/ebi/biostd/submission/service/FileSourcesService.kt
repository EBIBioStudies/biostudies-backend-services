package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.submission.helpers.FilesSourceFactory
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.io.sources.PreferredSource.FIRE
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.io.sources.PreferredSource.USER_SPACE
import ebi.ac.uk.security.integration.model.api.SecurityUser
import uk.ac.ebi.io.sources.DbFilesSource
import uk.ac.ebi.io.sources.FilesListSource
import uk.ac.ebi.io.sources.GroupSource
import uk.ac.ebi.io.sources.UserPathSource
import java.io.File

private val DEFAULT_SOURCES = listOf(USER_SPACE, SUBMISSION, FIRE)

class FileSourcesService(
    private val filesSourcesFactory: FilesSourceFactory,
) {
    fun submissionSources(rqt: FileSourcesRequest): FileSourcesList {
        val (owner, submitter, files, rootPath, submission, preferredSources) = rqt
        val preferred = preferredSources.ifEmpty { DEFAULT_SOURCES }
        val sources = buildList {
            if (submitter.superuser) add(DbFilesSource)
            if (files != null) add(FilesListSource(files))
            preferred.forEach {
                when (it) {
                    FIRE -> add(filesSourcesFactory.createFireSource())
                    USER_SPACE -> addUserSources(rootPath, owner, submitter, this)
                    SUBMISSION -> if (submission != null) add(filesSourcesFactory.createSubmissionSource(submission))
                }
            }
        }

        return FileSourcesList(sources)
    }

    private fun addUserSources(
        rootPath: String?,
        owner: SecurityUser?,
        submitter: SecurityUser,
        sources: MutableList<FilesSource>,
    ) {
        addUserSource(submitter, rootPath, sources)

        if (owner != null) {
            addUserSource(owner, rootPath, sources)
        }
    }

    private fun addUserSource(user: SecurityUser, rootPath: String?, sources: MutableList<FilesSource>) {
        if (rootPath == null) sources.add(UserPathSource("${user.email} user files", user.magicFolder.path))
        else sources.add(UserPathSource("${user.email} user files in /$rootPath", user.magicFolder.resolve(rootPath)))

        sources.addAll(user.groupsFolders.map { GroupSource(it.groupName, it.path) })
    }
}

data class FileSourcesRequest(
    val onBehalfUser: SecurityUser?,
    val submitter: SecurityUser,
    val files: List<File>?,
    val rootPath: String?,
    val submission: ExtSubmission?,
    val preferredSources: List<PreferredSource>,
)
