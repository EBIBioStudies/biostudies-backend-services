package uk.ac.ebi.scheculer.pmc.exporter.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import uk.ac.ebi.scheculer.pmc.exporter.model.PmcData
import uk.ac.ebi.scheculer.pmc.exporter.service.CHUNK_SIZE

fun pageIterator(pmcRepository: PmcRepository): Iterator<Page<PmcData>> =
    object : Iterator<Page<PmcData>> {
        var next = 0
        var currentPage: Page<PmcData> = Page.empty()

        override fun hasNext(): Boolean = if (next == 0) true else currentPage.hasNext()

        override fun next(): Page<PmcData> {
            if (!hasNext()) throw NoSuchElementException()

            currentPage = pmcRepository.findAllPmc(PageRequest.of(next, CHUNK_SIZE))
            next++

            return currentPage
        }
    }
