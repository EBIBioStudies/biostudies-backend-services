package uk.ac.ebi.io.config

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ebi.ac.uk.ftp.FtpClient
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.io.builder.FilesSourceListBuilder
import java.nio.file.Path

class FilesSourceConfig(
    private val submissionPath: Path,
    private val fireClient: FireClient,
    private val filesRepository: SubmissionFilesPersistenceService,
    private val ftpClient: FtpClient,
) {
    fun filesSourceListBuilder(): FilesSourceListBuilder = filesSourceListBuilder

    private val filesSourceListBuilder by lazy {
        FilesSourceListBuilder(submissionPath, fireClient, ftpClient, filesRepository)
    }
}
