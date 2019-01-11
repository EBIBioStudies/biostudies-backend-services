package ebi.ac.uk.model

import java.util.Objects

class File(var path: String, attributes: List<Attribute> = emptyList()) : Attributable(attributes) {

    override fun equals(other: Any?): Boolean {
        if (other !is File) return false
        if (this === other) return true

        return Objects.equals(this.path, other.path).and(
                Objects.equals(this.attributesMap, other.attributesMap))
    }

    override fun hashCode(): Int {
        return Objects.hash(this.path, this.attributesMap)
    }
}
