package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import org.springframework.web.bind.annotation.DeleteMapping
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
import uk.ac.ebi.fire.client.model.MetadataEntry
import java.io.File

/**
 * This resource exists only for testing purposes. Once the fire client is fully integrated with the submitter this
 * resource should be removed.
 */
@RestController
@RequestMapping("/fire/files")
class FireResource(
    private val fireWebClient: FireWebClient,
    private val tempFileGenerator: TempFileGenerator
) {
    @GetMapping
    fun findByPath(@RequestParam("path") path: String): FireFile? = fireWebClient.findByPath(path)

    @GetMapping("{accNo}")
    fun findByAccNo(
        @PathVariable accNo: String
    ): List<FireFile> = fireWebClient.findByMetadata(MetadataEntry("submissionAccNo", accNo))

    @GetMapping("/download")
    fun downloadByPath(@RequestParam("path") path: String): File = fireWebClient.downloadByPath(path)

    @PostMapping
    fun save(
        @RequestParam("file") multipartFile: MultipartFile,
        @RequestParam("path") path: String,
        @RequestParam("md5") md5: String,
        @RequestParam("accNo") accNo: String
    ): FireFile = fireWebClient.save(
        tempFileGenerator.asFile(multipartFile), path, md5, MetadataEntry("submissionAccNo", accNo))

    @PutMapping
    fun move(
        @RequestParam("source") source: String,
        @RequestParam("target") target: String
    ) = fireWebClient.move(source, target)

    @PutMapping("/{fireOid}/publish")
    fun publish(@PathVariable fireOid: String) = fireWebClient.publish(fireOid)

    @PutMapping("/{fireOid}/unpublish")
    fun unpublish(@PathVariable fireOid: String) = fireWebClient.unpublish(fireOid)

    @DeleteMapping("/{fireOid}")
    fun delete(@PathVariable fireOid: String) = fireWebClient.delete(fireOid)
}
