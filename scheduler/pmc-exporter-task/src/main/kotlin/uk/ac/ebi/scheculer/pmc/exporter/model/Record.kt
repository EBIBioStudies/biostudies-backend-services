package uk.ac.ebi.scheculer.pmc.exporter.model

private const val PMC_SOURCE = "PMC"

data class Record(
    val id: String
) {
    val source = PMC_SOURCE
}
