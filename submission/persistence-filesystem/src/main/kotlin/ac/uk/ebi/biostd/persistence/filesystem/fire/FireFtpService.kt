package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.ExtFireFile
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

        generateFtpLinks(accNo)

        logger.info { "$accNo $owner Finished publishing files of submission $accNo over FIRE" }
    }

    override fun unpublishSubmissionFiles(accNo: String, owner: String, relPath: String) {
        logger.info { "$accNo $owner Un-publishing files of submission $accNo over FIRE" }

        cleanFtpFolder(accNo)

        logger.info { "$accNo $owner Finished un-publishing files of submission $accNo over FIRE" }
    }

    override fun generateFtpLinks(accNo: String) {
        val sub = submissionQueryService.getExtByAccNo(accNo, includeFileListFiles = true)

        logger.info { "${sub.accNo} ${sub.owner} Started processing FTP links for submission $accNo over FIRE" }

        sub.allFiles()
            .filterIsInstance<ExtFireFile>()
            .forEach { publishFile(it.fireId) }

        logger.info { "${sub.accNo} ${sub.owner} Finished processing FTP links for submission $accNo over FIRE" }
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
