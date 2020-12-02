package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.filesystem.FtpFilesService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * This resource is just temporary to generate the FTP links
 */
@RestController
@RequestMapping("submissions/ftp")
class FtpResource(private val ftpFilesService: FtpFilesService) {
    @PostMapping("/generate")
    fun generateFtpLinks(@RequestParam("relPath", required = true) relPath: String) {
        ftpFilesService.createFtpFolder(relPath)
    }
}
