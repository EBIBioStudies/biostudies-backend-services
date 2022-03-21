package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.allFiles
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient

private val logger = KotlinLogging.logger {}

class FireFtpService(
    private val fireWebClient: FireWebClient,
    private val submissionQueryService: SubmissionQueryService
) : FtpService {
    override fun releaseSubmissionFiles(accNo: String, owner: String, relPath: String) {
        logger.info { "$accNo $owner Publishing files of submission $accNo over FIRE" }

        cleanFtpFolder(accNo)
        generateFtpLinks(accNo)

        logger.info { "$accNo $owner Finished publishing files of submission $accNo over FIRE" }
    }

    // TODO check FTP publishing
    override fun generateFtpLinks(accNo: String) {
        submissionQueryService
            .getExtByAccNo(accNo, includeFileListFiles = true)
            .allFiles()
            .filterIsInstance<FireFile>()
            .forEach { publishFile(it.fireId) }
    }

    private fun cleanFtpFolder(accNo: String) {
        fireWebClient
            .findByAccNoAndPublished(accNo, true)
            .forEach { unpublishFile(it.fireOid) }
    }

    private fun publishFile(fireId: String) {
        fireWebClient.publish(fireId)
        fireWebClient.setBioMetadata(fireId, published = true)
    }

    private fun unpublishFile(fireId: String) {
        fireWebClient.unpublish(fireId)
        fireWebClient.setBioMetadata(fireId, published = false)
    }
}
