package uk.ac.ebi.scheduler.exporter.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

internal const val PMC_SOURCE = "PMC"
internal const val PROVIDER_ID = 1518

@JacksonXmlRootElement(localName = "links")
data class Links(
    val link: List<Link>
)

data class Link(
    val resource: Resource,
    val record: Record
) {
    @JacksonXmlProperty(isAttribute = true)
    val providerId = PROVIDER_ID
}

data class Record(
    val id: String
) {
    val source = PMC_SOURCE
}

data class Resource(
    val url: String,
    val title: String
)
