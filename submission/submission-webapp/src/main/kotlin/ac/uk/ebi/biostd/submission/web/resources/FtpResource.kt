package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Help to generate ftp folder for a specific submission path.
 */
@RestController
@RequestMapping("submissions/ftp")
class FtpResource(private val ftpService: FtpService) {
    @PostMapping("/generate")
    fun generateFtpLinks(@RequestParam("relPath", required = true) relPath: String) {
        ftpService.createFtpFolder(relPath)
    }
}
