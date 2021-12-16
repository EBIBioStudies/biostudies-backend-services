package uk.ac.ebi.scheculer.pmc.exporter.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

private const val PROVIDER_ID = 1518

data class Link(
    val resource: Resource,
    val record: Record
) {
    @JacksonXmlProperty(isAttribute = true)
    val providerId = PROVIDER_ID
}

@JacksonXmlRootElement(localName = "links")
data class Links(
    val links: List<Link>
)
