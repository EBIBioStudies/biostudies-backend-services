package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.submission.web.model.ExtPage
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.net.URI

class ExtendedPageMapper(private val instanceBase: URI) {

    fun asExtPage(page: Page<ExtSubmission>): ExtPage =
        ExtPage(
            content = page.content,
            totalElements = page.totalElements,
            offset = page.pageable.offset,
            limit = page.pageable.pageSize,
            next = getNext(page),
            previous = getPrevious(page)
        )

    private fun getPrevious(page: Page<ExtSubmission>): String? =
        if (page.hasPrevious()) instanceBase.resolve(asQueryParams(page.previousPageable())).toString() else null

    private fun getNext(page: Page<ExtSubmission>): String? =
        if (page.hasNext()) instanceBase.resolve(asQueryParams(page.nextPageable())).toString() else null

    private fun asQueryParams(nextPageable: Pageable): String =
        "$instanceBase/submissions/extended?offset=${nextPageable.offset}&size=${nextPageable.pageSize}"
}
