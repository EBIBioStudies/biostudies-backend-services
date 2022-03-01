package ac.uk.ebi.biostd.client.extensions

import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.extended.model.ExtPage
import ebi.ac.uk.extended.model.ExtSubmission

fun BioWebClient.getExtSubmissionsAsSequence(extPageQuery: ExtPageQuery): Sequence<ExtSubmission> =
    pageIterator(extPageQuery)
        .asSequence()
        .map { it.content }
        .flatten()

private fun BioWebClient.pageIterator(query: ExtPageQuery): Iterator<ExtPage> =
    object : Iterator<ExtPage> {
        var firstPage: ExtPage? = getExtSubmissions(query)
        var currentPage = firstPage!!

        override fun hasNext(): Boolean = firstPage != null || currentPage.next.isNotBlank()

        override fun next(): ExtPage {
            require(hasNext()) { throw NoSuchElementException() }

            when (firstPage) {
                null -> currentPage = getExtSubmissionsPage(currentPage.next!!)
                else -> firstPage = null
            }

            return currentPage
        }
    }
