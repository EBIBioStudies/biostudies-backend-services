package uk.ac.ebi.io.builder

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allInnerSubmissionFiles
import ebi.ac.uk.ftp.FtpClient
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.SourcesList
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.security.integration.model.api.FtpUserFolder
import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.io.sources.DbFilesSource
import uk.ac.ebi.io.sources.FilesListSource
import uk.ac.ebi.io.sources.FtpSource
import uk.ac.ebi.io.sources.GroupPathSource
import uk.ac.ebi.io.sources.PathSource
import uk.ac.ebi.io.sources.SubmissionFilesSource
import uk.ac.ebi.io.sources.UserPathSource
import java.io.File
import java.nio.file.Path

@Suppress("LongParameterList")
class FilesSourceListBuilder(
    private val checkFilesPath: Boolean,
    private val submissionPath: Path,
    private val fireClient: FireClient,
    private val ftpClient: FtpClient,
    private val filesRepository: SubmissionFilesPersistenceService,
    private val sources: MutableList<FilesSource> = mutableListOf(),
) {
    private fun build(): FileSourcesList = SourcesList(checkFilesPath, sources.toList())

    fun buildFilesSourceList(builderAction: FilesSourceListBuilder.() -> Unit): FileSourcesList {
        this.sources.clear()
        return this.apply { builderAction() }.build()
    }

    fun addDbFilesSource() {
        sources.add(DbFilesSource)
    }

    fun addFilesListSource(files: List<File>) {
        sources.add(FilesListSource(files))
    }

    fun addUserSource(
        securityUser: SecurityUser,
        description: String,
        rootPath: String? = null,
    ) {
        val folder = securityUser.userFolder
        if (folder is NfsUserFolder) {
            val path = if (rootPath == null) folder.path else folder.path.resolve(rootPath)
            sources.add(UserPathSource(description, path))
        }

        if (folder is FtpUserFolder) {
            val ftpUrl = if (rootPath == null) folder.relativePath else folder.relativePath.resolve(rootPath)
            val nfsPath = if (rootPath == null) folder.path else folder.path.resolve(rootPath)
            sources.add(FtpSource(description, ftpUrl, nfsPath, ftpClient))
        }
    }

    fun addGroupSource(
        groupName: String,
        sourcePath: Path,
    ) {
        sources.add(GroupPathSource(groupName, sourcePath))
    }

    fun addSubmissionSource(submission: ExtSubmission) {
        val nfsSubPath = submissionPath.resolve("${submission.relPath}/$FILES_PATH")
        val nfsFiles = PathSource("Previous version files", nfsSubPath)
        val previousVersionFiles =
            submission
                .allInnerSubmissionFiles
                .groupBy { it.filePath }
                .mapValues { it.value.first() }
        sources.add(SubmissionFilesSource(submission, nfsFiles, fireClient, previousVersionFiles, filesRepository))
    }
}
