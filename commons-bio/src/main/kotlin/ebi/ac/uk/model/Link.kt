package ebi.ac.uk.model

import java.util.*

class Link(var url: String, attributes: List<Attribute> = emptyList()) : Attributable(attributes) {

    override fun equals(other: Any?): Boolean {
        if (other !is Link) return false
        if (this === other) return true

        return Objects.equals(this.url, other.url).and(
                Objects.equals(this.attributesMap, other.attributesMap))
    }

    override fun hashCode(): Int {
        return Objects.hash(this.url, this.attributesMap)
    }
}