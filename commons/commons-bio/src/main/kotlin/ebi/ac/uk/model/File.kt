package ebi.ac.uk.model

import java.util.Objects.equals
import java.util.Objects.hash

class File(
    var path: String,
    var size: Long = 0,
    override var attributes: List<Attribute> = listOf()
) : Attributable {

    override fun equals(other: Any?): Boolean {
        if (other !is File) return false
        if (this === other) return true

        return equals(this.path, other.path)
            .and(equals(this.size, other.size))
            .and(equals(this.attributes, other.attributes))
    }

    override fun hashCode(): Int = hash(this.path, this.size, this.attributes)
}
