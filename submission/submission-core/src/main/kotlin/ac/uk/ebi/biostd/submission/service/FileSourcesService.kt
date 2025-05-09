package ac.uk.ebi.biostd.submission.service

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.io.sources.PreferredSource.USER_SPACE
import ebi.ac.uk.paths.FolderType
import ebi.ac.uk.security.integration.model.api.SecurityUser
import uk.ac.ebi.io.builder.FilesSourceListBuilder
import java.io.File

private val DEFAULT_SOURCES = listOf(USER_SPACE, SUBMISSION)

class FileSourcesService(
    private val builder: FilesSourceListBuilder,
) {
    fun submissionSources(rqt: FileSourcesRequest): FileSourcesList {
        val folderType = rqt.folderType
        val onBehalfUser = rqt.onBehalfUser
        val submitter = rqt.submitter
        val files = rqt.files
        val rootPath = rqt.rootPath
        val submission = rqt.submission
        val preferred = rqt.preferredSources.ifEmpty { DEFAULT_SOURCES }

        val sources =
            builder.buildFilesSourceList {
                if (submitter.superuser) addDbFilesSource()
                if (files != null) addFilesListSource(files)
                preferred.forEach {
                    when (it) {
                        USER_SPACE -> addUserSources(rootPath, onBehalfUser, submitter, folderType, this)
                        SUBMISSION -> if (submission != null) addSubmissionSource(submission, folderType)
                    }
                }
            }

        return sources
    }

    private fun addUserSources(
        rootPath: String?,
        owner: SecurityUser?,
        submitter: SecurityUser,
        folderType: FolderType,
        builder: FilesSourceListBuilder,
    ) {
        addUserSource(submitter, rootPath, folderType, builder)

        if (owner != null) {
            addUserSource(owner, rootPath, folderType, builder)
        }
    }

    private fun addUserSource(
        user: SecurityUser,
        rootPath: String?,
        folderType: FolderType,
        builder: FilesSourceListBuilder,
    ) {
        if (rootPath == null) {
            builder.addUserSource(user, "${user.email} user files", folderType)
        } else {
            builder.addUserSource(user, "${user.email} user files in /$rootPath", folderType, rootPath)
        }

        user.groupsFolders.forEach { builder.addGroupSource(it.groupName, it.path) }
    }
}

data class FileSourcesRequest(
    val folderType: FolderType,
    val onBehalfUser: SecurityUser?,
    val submitter: SecurityUser,
    val files: List<File>?,
    val rootPath: String?,
    val submission: ExtSubmission?,
    val preferredSources: List<PreferredSource>,
)
