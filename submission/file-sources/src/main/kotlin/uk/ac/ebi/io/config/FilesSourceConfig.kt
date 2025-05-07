package uk.ac.ebi.io.config

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ebi.ac.uk.ftp.FtpClient
import ebi.ac.uk.paths.SubmissionFolderResolver
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.io.builder.FilesSourceListBuilder

class FilesSourceConfig(
    private val submissionFolderResolver: SubmissionFolderResolver,
    private val fireClient: FireClient,
    private val filesRepository: SubmissionFilesPersistenceService,
    private val userFtpClient: FtpClient,
    private val subFtpClient: FtpClient,
) {
    fun filesSourceListBuilder(): FilesSourceListBuilder = filesSourceListBuilder

    private val filesSourceListBuilder by lazy {
        FilesSourceListBuilder(
            submissionFolderResolver,
            fireClient,
            userFtpClient = userFtpClient,
            submissionFtpClient = subFtpClient,
            filesRepository = filesRepository,
        )
    }
}
