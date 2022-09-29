package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.forEachFile
import uk.ac.ebi.fire.client.integration.web.FireClient

private val logger = KotlinLogging.logger {}

class FireFtpService(
    private val fireClient: FireClient,
    private val serializationService: ExtSerializationService,
) : FtpService {
    override fun releaseSubmissionFile(file: ExtFile) {
        val fireFile = file as FireFile
        fireClient.publish(fireFile.fireId)
    }

//    override fun releaseSubmissionFiles(sub: ExtSubmission) {
//        logger.info { "${sub.accNo} ${sub.owner} Started processing FTP links for submission ${sub.accNo} over FIRE" }
//        serializationService.forEachFile(sub) { file, idx -> if (file is FireFile) publishFile(sub, file.fireId, idx) }
//        logger.info { "${sub.accNo} ${sub.owner} Finished processing FTP links for submission ${sub.accNo} over FIRE" }
//    }
//
//    private fun publishFile(sub: ExtSubmission, fireId: String, index: Int) {
//        logger.debug { "${sub.accNo}, ${sub.owner} publishing file $index, fireId='$fireId'" }
//        fireClient.publish(fireId)
//    }
}
