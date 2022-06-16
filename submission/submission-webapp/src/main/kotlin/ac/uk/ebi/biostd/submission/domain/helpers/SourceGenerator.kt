package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.model.GroupSource
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesListSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.io.sources.PreferredSource
import ebi.ac.uk.io.sources.PreferredSource.FIRE
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.io.sources.PreferredSource.USER_SPACE
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.security.integration.model.api.GroupMagicFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File
import java.nio.file.Paths

class SourceGenerator(
    private val props: ApplicationProperties,
    private val fireSourceFactory: FireFilesSourceFactory,
) {
    fun submissionSources(requestSources: RequestSources): FilesSource {
        val sources = buildList {
            add(FilesListSource(requestSources.files))

            if (requestSources.preferredSources.isEmpty()) {
                addDefaultFileSources(requestSources, this)
            } else {
                requestSources.preferredSources.forEach { addFileSource(it, requestSources, this) }
            }
        }

        return ComposedFileSource(sources)
    }

    fun submitterSources(
        user: SecurityUser,
        onBehalfUser: SecurityUser? = null,
        rootPath: String? = null
    ): FilesSource = ComposedFileSource(
        buildList {
            addUserSources(rootPath, user, onBehalfUser, this)
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
        submitter: SecurityUser?,
        sources: MutableList<FilesSource>
    ) {
        if (submitter != null) {
            sources.add(createPathSource(submitter, rootPath))
            sources.addAll(groupSources(submitter.groupsFolders))
        }

        if (owner != null) {
            sources.add(createPathSource(owner, rootPath))
            sources.addAll(groupSources(owner.groupsFolders))
        }
    }

    private fun addSubmissionSources(sub: ExtSubmission?, sources: MutableList<FilesSource>) {
        if (sub != null) {
            sources.add(fireSourceFactory.createSubmissionFireSource(sub.accNo, Paths.get("${sub.relPath}/Files")))
            sources.add(PathFilesSource(Paths.get(props.submissionPath).resolve(sub.relPath).resolve(FILES_PATH)))
        }
    }

    private fun addFireSources(sources: MutableList<FilesSource>) {
        if (props.persistence.enableFire) {
            sources.add(fireSourceFactory.createFireSource())
        }
    }

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
    val rootPath: String?,
    val submission: ExtSubmission?,
    val preferredSources: List<PreferredSource>
)
