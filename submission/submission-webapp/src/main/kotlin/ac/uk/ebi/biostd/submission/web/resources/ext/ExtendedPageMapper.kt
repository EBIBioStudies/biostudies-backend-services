package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.submission.web.model.ExtPage
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.util.web.optionalQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

class ExtendedPageMapper(private val instanceBase: URI) {
    fun asExtPage(page: Page<ExtSubmission>, request: ExtPageRequest): ExtPage =
        ExtPage(
            content = page.content,
            totalElements = page.totalElements,
            offset = page.pageable.offset,
            limit = page.pageable.pageSize,
            next = getNext(page, request),
            previous = getPrevious(page, request))

    private fun getPrevious(page: Page<ExtSubmission>, request: ExtPageRequest): String? =
        if (page.hasPrevious()) instanceBase.resolve(asUrl(page.previousPageable(), request)).toString() else null

    private fun getNext(page: Page<ExtSubmission>, request: ExtPageRequest): String? =
        if (page.hasNext()) instanceBase.resolve(asUrl(page.nextPageable(), request)).toString() else null

    private fun asUrl(pageable: Pageable, request: ExtPageRequest): String =
        UriComponentsBuilder.fromUriString("$instanceBase/submissions/extended")
            .queryParam("offset", pageable.offset)
            .queryParam("limit", pageable.pageSize)
            .optionalQueryParam("fromRTime", request.fromRTime)
            .optionalQueryParam("toRTime", request.toRTime)
            .optionalQueryParam("released", request.released)
            .build()
            .toUriString()
}
