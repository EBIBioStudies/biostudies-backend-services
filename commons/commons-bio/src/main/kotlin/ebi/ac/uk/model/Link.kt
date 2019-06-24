package ebi.ac.uk.model

import java.util.Objects

class Link(
    var url: String,
    override var attributes: List<Attribute> = listOf()) : Attributable {

    override fun equals(other: Any?) = when {
        other !is Link -> false
        other === this -> true
        else -> Objects.equals(this.url, other.url)
            .and(Objects.equals(this.attributes, other.attributes))
    }

    override fun hashCode() = Objects.hash(this.url, this.attributes)
}
