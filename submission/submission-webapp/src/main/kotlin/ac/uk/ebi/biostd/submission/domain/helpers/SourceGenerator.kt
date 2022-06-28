package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.submission.model.GroupSource
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.sources.FilesListSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.FilesSources
import ebi.ac.uk.io.sources.PathSource
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File
import java.nio.file.Paths

class SourceGenerator(
    private val props: ApplicationProperties,
    private val fireSourceFactory: FireFilesSourceFactory,
) {
    fun submissionSources(requestSources: RequestSources): FilesSources {
        val (onBehalfUser, submitter, files, rootPath, submission) = requestSources
        return FilesSources(submissionSources(onBehalfUser, submitter, files, rootPath, submission))
    }

    fun submitterSources(user: SecurityUser, onBehalfUser: SecurityUser? = null) = FilesSources(
        buildList {
            if (onBehalfUser !== null) {
                add(PathSource("${onBehalfUser.email} user files", onBehalfUser.magicFolder.path))
                addAll(onBehalfUser.groupsFolders.map { GroupSource(it.groupName, it.path) })
            }

            add(PathSource("${user.email} user files", user.magicFolder.path))
            addAll(user.groupsFolders.map { GroupSource(it.groupName, it.path) })
        }
    )

    private fun submissionSources(
        onBehalf: SecurityUser?,
        submitter: SecurityUser,
        files: List<File>?,
        rootPath: String?,
        sub: ExtSubmission?,
    ): List<FilesSource> {
        fun userSources(user: SecurityUser, rootPath: String?) = buildList {
            if (rootPath == null) add(PathSource("${user.email} user files", user.magicFolder.path))
            else add(PathSource("${user.email} user files in /$rootPath", user.magicFolder.resolve(rootPath)))
            addAll(user.groupsFolders.map { GroupSource(it.groupName, it.path) })
        }

        return buildList {
            if (files != null) {
                add(FilesListSource(files))
            }

            addAll(userSources(submitter, rootPath))

            if (onBehalf != null) {
                addAll(userSources(onBehalf, rootPath))
            }

            if (props.persistence.enableFire && sub == null) {
                add(fireSourceFactory.createFireSource())
            }

            if (sub != null) {
                add(fireSourceFactory.createSubmissionFireSource(sub.accNo, Paths.get("${sub.relPath}/Files")))

                val subPath = Paths.get(props.submissionPath).resolve("${sub.relPath}/$FILES_PATH")
                add(PathSource("Submission ${sub.accNo} previous version files", subPath))
            }
        }
    }
}

data class RequestSources(
    val onBehalfUser: SecurityUser?,
    val submitter: SecurityUser,
    val files: List<File>?,
    val rootPath: String?,
    val submission: ExtSubmission?,
)
