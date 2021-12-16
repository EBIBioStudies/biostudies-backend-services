package uk.ac.ebi.scheculer.pmc.exporter.mapping

import ebi.ac.uk.extended.model.ExtSubmission
import uk.ac.ebi.scheculer.pmc.exporter.model.Link
import uk.ac.ebi.scheculer.pmc.exporter.model.Record
import uk.ac.ebi.scheculer.pmc.exporter.model.Resource

internal const val PMC_PATTERN = "S-E"
internal const val BIOSTUDIES_URL = "http://www.ebi.ac.uk/biostudies/studies"

object LinkMapper {
    fun ExtSubmission.toLink(): Link {
        val record = Record(accNo.substringAfter(PMC_PATTERN))
        val resource = Resource("$BIOSTUDIES_URL/$accNo?xr=true", title.orEmpty())

        return Link(resource, record)
    }
}
