package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.submission.web.model.ExtPage
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.net.URI

class ExtendedPageMapper(private val instanceBase: URI) {
    fun asExtPage(page: Page<ExtSubmission>, request: ExtPageRequest): ExtPage =
        ExtPage(
            content = page.content,
            totalElements = page.totalElements,
            offset = page.pageable.offset,
            limit = page.pageable.pageSize,
            next = getNext(page, request),
            previous = getPrevious(page, request)
        )

    private fun getPrevious(page: Page<ExtSubmission>, request: ExtPageRequest): String? =
        if (page.hasPrevious()) instanceBase.resolve(asUrl(page.previousPageable(), request)).toString() else null

    private fun getNext(page: Page<ExtSubmission>, request: ExtPageRequest): String? =
        if (page.hasNext()) instanceBase.resolve(asUrl(page.nextPageable(), request)).toString() else null

    private fun asUrl(next: Pageable, request: ExtPageRequest): String {
        val url = StringBuilder("$instanceBase/submissions/extended?offset=${next.offset}&limit=${next.pageSize}")
        request.fromRTime?.let { url.append("&fromRTime=$it") }
        request.toRTime?.let { url.append("&fromRTime=$it") }

        return url.toString()
    }
}
