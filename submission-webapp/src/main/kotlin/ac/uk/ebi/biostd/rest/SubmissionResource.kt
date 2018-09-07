package ac.uk.ebi.biostd.rest

import ac.uk.ebi.biostd.service.SubmissionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
class SubmissionResource(private val submissionService: SubmissionService) {

    @GetMapping("/{id}.json", produces = ["application/json; charset=utf-8"])
    @ResponseBody
    fun asJson(@PathVariable id: Long): String {
        return submissionService.getSubmissionAsJson(id)
    }

    @GetMapping("/{id}.xml", produces = ["application/xml; charset=utf-8"])
    fun asXml(@PathVariable id: Long): String {
        return submissionService.getSubmissionAsXml(id)
    }

    @GetMapping("/{id}.tsv", produces = ["text/plain; charset=utf-8"])
    fun asTsv(@PathVariable id: Long): String {
        return submissionService.getSubmissionAsTsv(id)
    }
}
