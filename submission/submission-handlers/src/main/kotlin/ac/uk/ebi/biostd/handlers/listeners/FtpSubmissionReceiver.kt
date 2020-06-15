package ac.uk.ebi.biostd.handlers.listeners

import ac.uk.ebi.biostd.handlers.FTP_QUEUE
import ebi.ac.uk.extended.events.SubmissionSubmitted
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.FileUtils
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

val ALL_READ: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-xr-x")

class FtpSubmissionReceiver(
    private val ftpFolder: Path,
    private val submissionFolder: Path,
    private val restTemplate: RestTemplate
) {

    @RabbitListener(queues = [FTP_QUEUE])
    fun receiveMessage(message: SubmissionSubmitted) {
        val submission = restTemplate.getForObject<ExtSubmission>(message.extTabUrl)
        if (submission.released) {
            val ftpFolder = ftpFolder.resolve(submission.relPath)
            FileUtils.createEmptyFolder(ftpFolder, ALL_READ)

            val submissionFolder = submissionFolder.resolve(submission.relPath)
            FileUtils.createHardLink(submissionFolder.toFile(), ftpFolder.toFile())
        }
    }
}
