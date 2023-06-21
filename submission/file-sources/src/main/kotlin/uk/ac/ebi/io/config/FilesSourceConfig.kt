package uk.ac.ebi.io.config

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.io.builder.FilesSourceListBuilder
import java.nio.file.Path

class FilesSourceConfig(
    internal val submissionPath: Path,
    internal val fireClient: FireClient,
    internal val filesRepository: SubmissionFilesPersistenceService,
) {
    fun filesSourceListBuilder(): FilesSourceListBuilder = filesSourceListBuilder

    private val filesSourceListBuilder by lazy {
        FilesSourceListBuilder(submissionPath, fireClient, filesRepository)
    }
}
