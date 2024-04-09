package ac.uk.ebi.biostd.submission.service

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.io.sources.PreferredSource.USER_SPACE
import ebi.ac.uk.security.integration.model.api.SecurityUser
import uk.ac.ebi.io.builder.FilesSourceListBuilder
import java.io.File

private val DEFAULT_SOURCES = listOf(USER_SPACE, SUBMISSION)

class FileSourcesService(
    private val builder: FilesSourceListBuilder,
) {
    fun submissionSources(rqt: FileSourcesRequest): FileSourcesList {
        val (owner, submitter, files, rootPath, submission, preferredSources) = rqt
        val preferred = preferredSources.ifEmpty { DEFAULT_SOURCES }
        val sources =
            builder.buildFilesSourceList {
                if (submitter.superuser) addDbFilesSource()
                if (files != null) addFilesListSource(files)
                preferred.forEach {
                    when (it) {
                        USER_SPACE -> addUserSources(rootPath, owner, submitter, this)
                        SUBMISSION -> if (submission != null) addSubmissionSource(submission)
                    }
                }
            }

        return sources
    }

    private fun addUserSources(
        rootPath: String?,
        owner: SecurityUser?,
        submitter: SecurityUser,
        builder: FilesSourceListBuilder,
    ) {
        addUserSource(submitter, rootPath, builder)

        if (owner != null) {
            addUserSource(owner, rootPath, builder)
        }
    }

    private fun addUserSource(
        user: SecurityUser,
        rootPath: String?,
        builder: FilesSourceListBuilder,
    ) {
        if (rootPath == null) {
            builder.addUserSource(user, "${user.email} user files")
        } else {
            builder.addUserSource(user, "${user.email} user files in /$rootPath", rootPath)
        }

        user.groupsFolders.forEach { builder.addGroupSource(it.groupName, it.path) }
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
