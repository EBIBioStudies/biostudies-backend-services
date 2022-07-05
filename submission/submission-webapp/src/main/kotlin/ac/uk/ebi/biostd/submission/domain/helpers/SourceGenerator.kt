package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.model.GroupSource
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.io.sources.FilesListSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathSource
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.io.sources.PreferredSource.FIRE
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.io.sources.PreferredSource.USER_SPACE
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File
import java.nio.file.Paths

class SourceGenerator(
    private val props: ApplicationProperties,
    private val fireSourceFactory: FireFilesSourceFactory,
) {
    fun submissionSources(requestSources: RequestSources): FileSourcesList {
        val sources = buildList {
            if (requestSources.files != null) {
                add(FilesListSource(requestSources.files))
            }

            if (requestSources.preferredSources.isEmpty()) {
                addDefaultFileSources(requestSources, this)
            } else {
                requestSources.preferredSources.forEach { addFileSource(it, requestSources, this) }
            }
        }

        return FileSourcesList(sources)
    }

    fun submitterSources(
        submitter: SecurityUser,
        onBehalfUser: SecurityUser? = null,
        rootPath: String? = null
    ): FileSourcesList = FileSourcesList(
        buildList {
            addUserSources(rootPath, onBehalfUser, submitter, this)
        }
    )

    private fun addFileSource(
        type: PreferredSource,
        requestSources: RequestSources,
        sources: MutableList<FilesSource>
    ) {
        val (owner, submitter, _, rootPath, submission, _) = requestSources

        when (type) {
            FIRE -> addFireSources(sources)
            USER_SPACE -> addUserSources(rootPath, owner, submitter, sources)
            SUBMISSION -> addSubmissionSources(submission, sources)
        }
    }

    private fun addDefaultFileSources(requestSources: RequestSources, sources: MutableList<FilesSource>) {
        val (owner, submitter, _, rootPath, submission, _) = requestSources

        addUserSources(rootPath, owner, submitter, sources)
        addSubmissionSources(submission, sources)
        addFireSources(sources)
    }

    private fun addUserSources(
        rootPath: String?,
        owner: SecurityUser?,
        submitter: SecurityUser,
        sources: MutableList<FilesSource>
    ) {
        addUserSource(submitter, rootPath, sources)

        if (owner != null) {
            addUserSource(owner, rootPath, sources)
        }
    }

    private fun addUserSource(user: SecurityUser, rootPath: String?, sources: MutableList<FilesSource>) {
        if (rootPath == null) sources.add(PathSource("${user.email} user files", user.magicFolder.path))
        else sources.add(PathSource("${user.email} user files in /$rootPath", user.magicFolder.resolve(rootPath)))

        sources.addAll(user.groupsFolders.map { GroupSource(it.groupName, it.path) })
    }

    private fun addSubmissionSources(sub: ExtSubmission?, sources: MutableList<FilesSource>) {
        if (sub != null) {
            val subPath = Paths.get(props.submissionPath).resolve("${sub.relPath}/$FILES_PATH")

            sources.add(fireSourceFactory.createSubmissionFireSource(sub.accNo, Paths.get("${sub.relPath}/Files")))
            sources.add(PathSource("Submission ${sub.accNo} previous version files", subPath))
        }
    }

    private fun addFireSources(sources: MutableList<FilesSource>) {
        if (props.persistence.enableFire) {
            sources.add(fireSourceFactory.createFireSource())
        }
    }
}

data class RequestSources(
    val onBehalfUser: SecurityUser?,
    val submitter: SecurityUser,
    val files: List<File>?,
    val rootPath: String?,
    val submission: ExtSubmission?,
    val preferredSources: List<PreferredSource>
)
