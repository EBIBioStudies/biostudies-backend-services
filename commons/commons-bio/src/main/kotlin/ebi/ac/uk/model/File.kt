package ebi.ac.uk.model

import java.util.Objects

class File(
    var path: String,
    var size: Long = 0,
    override var attributes: List<Attribute> = listOf()) : Attributable {

    override fun equals(other: Any?): Boolean {
        if (other !is File) return false
        if (this === other) return true

        return Objects.equals(this.path, other.path)
            .and(Objects.equals(this.size, other.size))
            .and(Objects.equals(this.attributes, other.attributes))
    }

    override fun hashCode(): Int = Objects.hash(this.path, this.size, this.attributes)
}
