package ac.uk.ebi.biostd.submission.helpers

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allInnerSubmissionFiles
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathSource
import ebi.ac.uk.paths.FILES_PATH
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.nio.file.Paths

class FilesSourceFactory(
    private val fireClient: FireClient,
    private val applicationProperties: ApplicationProperties,
    private val queryService: SubmissionPersistenceQueryService,
) {
    fun createFireSource(): FilesSource = FireFilesSource(fireClient)

    fun createSubmissionSource(sub: ExtSubmission): FilesSource {
        val nfsSubPath = Paths.get(applicationProperties.submissionPath).resolve("${sub.relPath}/$FILES_PATH")
        val nfsFiles = PathSource("Previous version files", nfsSubPath)
        val previousVersionFiles = sub
            .allInnerSubmissionFiles
            .groupBy { it.filePath }
            .mapValues { it.value.first() }

        return SubmissionFilesSource(sub.accNo, sub.version, nfsFiles, fireClient, previousVersionFiles, queryService)
    }
}
