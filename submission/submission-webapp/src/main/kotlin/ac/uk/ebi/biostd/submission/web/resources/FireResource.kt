package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.FireFile
import java.io.File

/**
 * This resource exists only for testing purposes. Once the fire client is fully integrated with the submitter this
 * resource should be removed.
 */
@RestController
@Suppress("UnusedPrivateMember")
@RequestMapping("/fire/files")
class FireResource(
    private val fireWebClient: FireWebClient,
    private val tempFileGenerator: TempFileGenerator
) {
    @GetMapping("/download")
    fun downloadByPath(@RequestParam("path") path: String): File = fireWebClient.downloadByPath(path)

    @PostMapping
    fun save(
        @RequestParam("file") multipartFile: MultipartFile,
        @RequestParam("path") path: String,
        @RequestParam("md5") md5: String,
        @RequestParam("accNo") accNo: String
    ): FireFile {
        val persisted = fireWebClient.save(tempFileGenerator.asFile(multipartFile), md5)
        fireWebClient.setPath(persisted.fireOid, path)
        return persisted
    }

    @PutMapping("/{fireOid}/publish")
    fun publish(@PathVariable fireOid: String) = fireWebClient.publish(fireOid)

    @PutMapping("/{fireOid}/unpublish")
    fun unpublish(@PathVariable fireOid: String) = fireWebClient.unpublish(fireOid)
}
