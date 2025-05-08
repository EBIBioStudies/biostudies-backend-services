package uk.ac.ebi.io.builder

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allInnerSubmissionFiles
import ebi.ac.uk.ftp.FtpClient
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.SourcesList
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.FolderType
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.security.integration.model.api.FtpUserFolder
import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import mu.KotlinLogging
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

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class FilesSourceListBuilder(
    private val folderResolver: SubmissionFolderResolver,
    private val fireClient: FireClient,
    private val userFtpClient: FtpClient,
    private val submissionFtpClient: FtpClient,
    private val filesRepository: SubmissionFilesPersistenceService,
    private val sources: MutableList<FilesSource> = mutableListOf(),
) {
    private fun build(): FileSourcesList = SourcesList(sources.toList())

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
        folderType: FolderType,
        rootPath: String? = null,
    ) {
        val folder = securityUser.userFolder
        if (folder is NfsUserFolder) {
            val path = if (rootPath == null) folder.path else folder.path.resolve(rootPath)
            sources.add(UserPathSource(description, path))
        }

        if (folder is FtpUserFolder) {
            val nfsPath = if (rootPath == null) folder.path else folder.path.resolve(rootPath)
            when (folderType) {
                FolderType.NFS -> {
                    logger.info { "Adding user nfs files source in $nfsPath" }
                    sources.add(UserPathSource(description, nfsPath))
                }

                FolderType.FTP -> {
                    val ftpUrl = if (rootPath == null) folder.relativePath else folder.relativePath.resolve(rootPath)
                    logger.info {
                        "Adding user ftp files source in " +
                            "nfsPath='$nfsPath' " +
                            "ftpUrl='${userFtpClient.ftpRootPath}/$ftpUrl'"
                    }
                    sources.add(FtpSource(description, ftpUrl, nfsPath, userFtpClient))
                }
            }
        }
    }

    fun addGroupSource(
        groupName: String,
        sourcePath: Path,
    ) {
        sources.add(GroupPathSource(groupName, sourcePath))
    }

    fun addSubmissionSource(
        submission: ExtSubmission,
        folderType: FolderType,
    ) {
        fun submissionSource(): FilesSource =
            when (folderType) {
                FolderType.NFS -> {
                    val nfsSubPath = folderResolver.getSubmisisonFolder(submission, FolderType.NFS).resolve(FILES_PATH)
                    PathSource("Previous version files [File System]", nfsSubPath)
                }

                FolderType.FTP -> {
                    val ftpUrl = folderResolver.getSubmisisonFolder(submission, FolderType.FTP).resolve(FILES_PATH)
                    val nfsPath = folderResolver.getSubmisisonFolder(submission, FolderType.NFS).resolve(FILES_PATH)

                    logger.info {
                        "Adding submission ftp files source in " +
                            "nfsPath='$nfsPath' " +
                            "ftpUrl='${submissionFtpClient.ftpRootPath}/$ftpUrl'"
                    }

                    FtpSource("Previous version files [FTP]", ftpUrl = ftpUrl, nfsPath = nfsPath, submissionFtpClient)
                }
            }

        val submissionSource = submissionSource()
        val previousVersionFiles =
            submission
                .allInnerSubmissionFiles
                .groupBy { it.filePath }
                .mapValues { it.value.first() }
        sources.add(
            SubmissionFilesSource(
                sub = submission,
                fileSource = submissionSource,
                fireClient = fireClient,
                previousVersionFiles = previousVersionFiles,
                filesRepository = filesRepository,
            ),
        )
    }
}
