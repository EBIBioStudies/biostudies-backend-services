package ebi.ac.uk.model

import java.util.*

class File(var name: String, attributes: List<Attribute> = emptyList()) : Attributable(attributes) {

    override fun equals(other: Any?): Boolean {
        if (other !is File) return false
        if (this === other) return true

        return Objects.equals(this.name, other.name).and(
                Objects.equals(this.attributesMap, other.attributesMap))
    }

    override fun hashCode(): Int {
        return Objects.hash(this.name, this.attributesMap)
    }
}