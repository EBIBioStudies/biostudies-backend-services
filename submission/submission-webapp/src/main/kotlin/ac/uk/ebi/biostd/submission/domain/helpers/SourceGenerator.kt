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

private val DEFAULT_SOURCES = listOf(USER_SPACE, SUBMISSION, FIRE)

class SourceGenerator(
    private val props: ApplicationProperties,
    private val fireSourceFactory: FireFilesSourceFactory,
) {
    fun submissionSources(requestSources: RequestSources): FileSourcesList {
        val (owner, submitter, files, rootPath, submission, preferredSources) = requestSources
        val preferred = preferredSources.ifEmpty { DEFAULT_SOURCES }
        val sources = buildList {
            if (files != null) {
                add(FilesListSource(files))
            }

            preferred.forEach {
                when (it) {
                    FIRE -> if (props.persistence.enableFire) add(fireSourceFactory.createFireSource())
                    USER_SPACE -> addUserSources(rootPath, owner, submitter, this)
                    SUBMISSION -> addSubmissionSources(submission, this)
                }
            }
        }

        return FileSourcesList(sources)
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
}

data class RequestSources(
    val onBehalfUser: SecurityUser?,
    val submitter: SecurityUser,
    val files: List<File>?,
    val rootPath: String?,
    val submission: ExtSubmission?,
    val preferredSources: List<PreferredSource>
)
