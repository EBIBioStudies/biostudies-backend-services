package ebi.ac.uk.model

import java.util.Objects

class Link(var url: String, attributes: List<Attribute> = emptyList()) : Attributable(attributes) {

    override fun equals(other: Any?) = when{
        other !is Link -> false
        other === this -> true
        else -> Objects.equals(this.url, other.url).and(Objects.equals(this.attributesMap, other.attributesMap))
    }

    override fun hashCode() = Objects.hash(this.url, this.attributesMap)
}