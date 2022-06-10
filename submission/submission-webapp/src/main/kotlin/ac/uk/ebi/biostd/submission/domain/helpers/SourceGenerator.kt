package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.model.GroupSource
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesListSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File
import java.nio.file.Paths

class SourceGenerator(
    private val props: ApplicationProperties,
    private val fireSourceFactory: FireFilesSourceFactory,
) {
    fun submissionSources(requestSources: RequestSources): FilesSource {
        val (owner, submitter, files, rootPath, submission) = requestSources
        return ComposedFileSource(submissionSources(owner, submitter, files, rootPath, submission))
    }

    fun submitterSources(user: SecurityUser, onBehalfUser: SecurityUser? = null) = ComposedFileSource(
        buildList {
            if (onBehalfUser !== null) {
                add(PathFilesSource(onBehalfUser.magicFolder.resolve(null.orEmpty())))
                addAll(onBehalfUser.groupsFolders.map { GroupSource(it.groupName, it.path) })
            }

            add(PathFilesSource(user.magicFolder.resolve(null.orEmpty())))
            addAll(user.groupsFolders.map { GroupSource(it.groupName, it.path) })
        }
    )

    private fun submissionSources(
        owner: SecurityUser?,
        submitter: SecurityUser?,
        files: List<File>,
        rootPath: String?,
        sub: ExtSubmission?,
    ): List<FilesSource> {
        return buildList {
            add(FilesListSource(files))

            if (submitter != null) {
                add(PathFilesSource(submitter.magicFolder.resolve(rootPath.orEmpty())))
                addAll(submitter.groupsFolders.map { GroupSource(it.groupName, it.path) })
            }

            if (owner != null) {
                add(PathFilesSource(owner.magicFolder.resolve(rootPath.orEmpty())))
                addAll(owner.groupsFolders.map { GroupSource(it.groupName, it.path) })
            }

            if (props.persistence.enableFire && sub == null) {
                add(fireSourceFactory.createFireSource())
            }

            if (sub != null) {
                add(fireSourceFactory.createSubmissionFireSource(sub.accNo, Paths.get("${sub.relPath}/Files")))
                add(PathFilesSource(Paths.get(props.submissionPath).resolve(sub.relPath).resolve(FILES_PATH)))
            }
        }
    }
}

data class RequestSources(
    val owner: SecurityUser? = null,
    val submitter: SecurityUser? = null,
    val files: List<File> = emptyList(),
    val rootPath: String?,
    val submission: ExtSubmission?,
)
