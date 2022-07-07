package uk.ac.ebi.scheduler.pmc.exporter.mapping

import uk.ac.ebi.scheduler.pmc.exporter.model.Link
import uk.ac.ebi.scheduler.pmc.exporter.model.PmcData
import uk.ac.ebi.scheduler.pmc.exporter.model.Record
import uk.ac.ebi.scheduler.pmc.exporter.model.Resource

internal const val PMC_PATTERN = "S-E"
internal const val BIOSTUDIES_URL = "http://www.ebi.ac.uk/biostudies/studies"

fun PmcData.toLink(): Link {
    val record = Record(accNo.substringAfter(PMC_PATTERN))
    val resource = Resource("$BIOSTUDIES_URL/$accNo?xr=true", title.orEmpty())

    return Link(resource, record)
}
