package ac.uk.ebi.biostd.persistence.model

import java.util.Objects
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@Entity
class SectionAttribute(attribute: Attribute) :
    Attribute(attribute.name, attribute.value, attribute.order, attribute.reference) {
    init {
        this.valueQualifier = attribute.valueQualifier
        this.nameQualifier = attribute.valueQualifier
    }
}

@Entity
class LinkAttribute(attribute: Attribute) :
    Attribute(attribute.name, attribute.value, attribute.order, attribute.reference) {
    init {
        this.valueQualifier = attribute.valueQualifier
        this.nameQualifier = attribute.valueQualifier
    }
}

@Entity
class FileAttribute(attribute: Attribute) :
    Attribute(attribute.name, attribute.value, attribute.order, attribute.reference) {
    init {
        this.valueQualifier = attribute.valueQualifier
        this.nameQualifier = attribute.valueQualifier
    }
}

@Entity
class SubmissionAttribute(attribute: Attribute) :
    Attribute(attribute.name, attribute.value, attribute.order, attribute.reference) {
    init {
        this.valueQualifier = attribute.valueQualifier
        this.nameQualifier = attribute.valueQualifier
    }
}

@MappedSuperclass
open class Attribute(

    @Column
    val name: String,

    @Column
    val value: String,

    @Column(name = "ord")
    val order: Int
) : Comparable<Attribute> {

    override fun compareTo(other: Attribute) = order.compareTo(other.order)

    constructor(name: String, value: String, order: Int, reference: Boolean) : this(name, value, order) {
        this.reference = reference
    }

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column(name = "reference")
    var reference: Boolean = false

    @Column(name = "nameQualifierString")
    var nameQualifier: String? = null

    @Column(name = "valueQualifierString")
    var valueQualifier: String? = null

    override fun equals(other: Any?): Boolean {
        if (other !is Attribute) return false
        if (this === other) return true

        return Objects.equals(this.name, other.name)
            .and(Objects.equals(this.value, other.value))
    }

    override fun hashCode(): Int {
        return Objects.hash(this.name, this.value, this.order)
    }
}
