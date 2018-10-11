package ac.uk.ebi.biostd.serialization.rest

import ac.uk.ebi.biostd.serialization.service.SerializationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
class SubmissionResource(private val submissionService: SerializationService) {

    @GetMapping("/{accNo}.json", produces = ["application/json; charset=utf-8"])
    @ResponseBody
    fun asJson(@PathVariable accNo: String): String {
        return submissionService.getSubmissionAsJson(accNo)
    }

    @GetMapping("/{accNo}.xml", produces = ["application/xml; charset=utf-8"])
    fun asXml(@PathVariable accNo: String): String {
        return submissionService.getSubmissionAsXml(accNo)
    }

    @GetMapping("/{accNo}.tsv", produces = ["text/plain; charset=utf-8"])
    fun asTsv(@PathVariable accNo: String): String {
        return submissionService.getSubmissionAsTsv(accNo)
    }
}
