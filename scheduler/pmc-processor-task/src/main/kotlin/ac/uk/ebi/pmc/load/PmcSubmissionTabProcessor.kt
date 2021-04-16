package ac.uk.ebi.pmc.load

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import arrow.core.Either
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allSections

/**
 * Perform any submission changes required to update PMC generated representation.
 */
class PmcSubmissionTabProcessor(private val serializationService: SerializationService) {

    fun transformSubmission(originalPagetab: String): Submission {
        val submission = serializationService.deserializeSubmission(originalPagetab, SubFormat.TSV)
        submission.allSections().forEach { it.links = it.links.map { either -> updateLinks(either) }.toMutableList() }
        return submission
    }

    private fun updateLinks(either: Either<Link, LinksTable>) =
        either.bimap(this::transformLink, this::transformLinksTable)

    private fun transformLink(links: Link): Link {
        links.attributes.filter { it.value == "gen" }.forEach { it.value = "ENA" }
        return links
    }

    private fun transformLinksTable(links: LinksTable): LinksTable {
        links.elements.forEach { transformLink(it) }
        return links
    }
}
