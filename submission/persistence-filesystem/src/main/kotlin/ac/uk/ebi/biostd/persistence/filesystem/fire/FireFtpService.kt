package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.FireFile
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.forEachSubmissionFile
import uk.ac.ebi.fire.client.integration.web.FireClient

private val logger = KotlinLogging.logger {}

class FireFtpService(
    private val fireClient: FireClient,
    private val serializationService: ExtSerializationService,
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

        serializationService.forEachSubmissionFile(sub) { if (it is FireFile) publishFile(it.fireId) }
        logger.info { "${sub.accNo} ${sub.owner} Finished processing FTP links for submission $accNo over FIRE" }
    }

    private fun cleanFtpFolder(accNo: String) {
        fireClient
            .findByAccNoAndPublished(accNo, true)
            .forEach { unPublishFile(it.fireOid) }
    }

    private fun publishFile(fireId: String) {
        fireClient.publish(fireId)
        fireClient.setBioMetadata(fireId, published = true)
    }

    private fun unPublishFile(fireId: String) {
        fireClient.unpublish(fireId)
        fireClient.setBioMetadata(fireId, published = false)
    }
}
