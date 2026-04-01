package ac.uk.ebi.biostd.submission.web.resources.ext.mapping

import ac.uk.ebi.biostd.submission.domain.extended.ExtSubPageRequest
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.WebExtPage
import ebi.ac.uk.util.web.optionalQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

class ExtendedSubmissionPageMapper(
    private val instanceBase: URI,
) {
    fun asExtPage(
        page: Page<ExtSubmission>,
        request: ExtSubPageRequest,
    ): WebExtPage<ExtSubmission> =
        WebExtPage(
            content = page.content,
            totalElements = page.totalElements,
            offset = page.pageable.offset,
            limit = page.pageable.pageSize,
            next = getNext(page, request),
            previous = getPrevious(page, request),
        )

    private fun getPrevious(
        page: Page<ExtSubmission>,
        request: ExtSubPageRequest,
    ): String? = if (page.hasPrevious()) instanceBase.resolve(asUrl(page.previousPageable(), request)).toString() else null

    private fun getNext(
        page: Page<ExtSubmission>,
        request: ExtSubPageRequest,
    ): String? = if (page.hasNext()) instanceBase.resolve(asUrl(page.nextPageable(), request)).toString() else null

    private fun asUrl(
        pageable: Pageable,
        request: ExtSubPageRequest,
    ): String =
        UriComponentsBuilder
            .fromUriString("$instanceBase/submissions/extended")
            .queryParam("offset", pageable.offset)
            .queryParam("limit", pageable.pageSize)
            .optionalQueryParam("fromRTime", request.fromRTime)
            .optionalQueryParam("toRTime", request.toRTime)
            .optionalQueryParam("released", request.released)
            .optionalQueryParam("collection", request.collection)
            .build()
            .toUriString()
}
