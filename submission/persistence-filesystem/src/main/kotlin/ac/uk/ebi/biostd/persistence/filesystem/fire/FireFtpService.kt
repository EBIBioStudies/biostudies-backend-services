package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import uk.ac.ebi.fire.client.integration.web.FireClient

class FireFtpService(
    private val fireClient: FireClient,
) : FtpService {
    override fun releaseSubmissionFile(file: ExtFile, subRelPath: String): FireFile {
        val fireFile = file as FireFile
        val apiFile = fireClient.publish(fireFile.fireId)
        return fireFile.copy(firePath = apiFile.path, published = apiFile.published)
    }
}
