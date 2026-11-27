package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.ExtSubmissionInfo
import uk.ac.ebi.biostd.extended.model.FireFile
import uk.ac.ebi.biostd.fire.client.integration.web.FireClient

class FireFtpService(
    private val fireClient: FireClient,
) : FtpService {
    override suspend fun releaseSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): FireFile {
        require(file is FireFile) { "FireFtpService should handle only Fire Files" }
        val apiFile = fireClient.publish(file.fireId)
        return file.copy(firePath = apiFile.path!!, published = apiFile.published)
    }

    override suspend fun unReleaseSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): FireFile {
        require(file is FireFile) { "FireFtpService should handle only Fire Files" }
        val apiFile = fireClient.unpublish(file.fireId)
        return file.copy(firePath = apiFile.path!!, published = apiFile.published)
    }
}
