package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.converters.AttributeDetailConverter
import java.util.Objects
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@Entity
class DbSectionAttribute(attribute: DbAttribute) :
    DbAttribute(attribute.name, attribute.value, attribute.order, attribute.reference) {
    init {
        this.nameQualifier = attribute.nameQualifier
        this.valueQualifier = attribute.valueQualifier
    }
}

@Entity
class DbLinkAttribute(attribute: DbAttribute) :
    DbAttribute(attribute.name, attribute.value, attribute.order, attribute.reference) {
    init {
        this.nameQualifier = attribute.nameQualifier
        this.valueQualifier = attribute.valueQualifier
    }
}

@Entity
class DbFileAttribute(attribute: DbAttribute) :
    DbAttribute(attribute.name, attribute.value, attribute.order, attribute.reference) {
    init {
        this.nameQualifier = attribute.nameQualifier
        this.valueQualifier = attribute.valueQualifier
    }
}

@Entity
class DbReferencedFileAttribute(attribute: DbAttribute) :
    DbAttribute(attribute.name, attribute.value, attribute.order, attribute.reference) {
    init {
        this.nameQualifier = attribute.nameQualifier
        this.valueQualifier = attribute.valueQualifier
    }
}

@Entity
class DbSubmissionAttribute(attribute: DbAttribute) :
    DbAttribute(attribute.name, attribute.value, attribute.order, attribute.reference) {
    init {
        this.nameQualifier = attribute.nameQualifier
        this.valueQualifier = attribute.valueQualifier
    }
}

data class AttributeDetail(val name: String, val value: String)

@MappedSuperclass
open class DbAttribute(

    @Column
    val name: String,

    @Column
    val value: String,

    @Column(name = "ord")
    val order: Int
) : Comparable<DbAttribute> {

    override fun compareTo(other: DbAttribute) = order.compareTo(other.order)

    constructor(name: String, value: String, order: Int, reference: Boolean) : this(name, value, order) {
        this.reference = reference
    }

    constructor(
        name: String,
        value: String,
        order: Int,
        reference: Boolean,
        nameAttrs: List<AttributeDetail>,
        valueAttrs: List<AttributeDetail>
    ) :
        this(name, value, order, reference) {
        nameQualifier = nameAttrs.toMutableList()
        valueQualifier = valueAttrs.toMutableList()
    }

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column(name = "reference")
    var reference: Boolean = false

    @Convert(converter = AttributeDetailConverter::class)
    @Column(name = "nameQualifierString")
    var nameQualifier: MutableList<AttributeDetail> = mutableListOf()

    @Convert(converter = AttributeDetailConverter::class)
    @Column(name = "valueQualifierString")
    var valueQualifier: MutableList<AttributeDetail> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (other !is DbAttribute) return false
        if (this === other) return true

        return Objects.equals(this.name, other.name)
            .and(Objects.equals(this.value, other.value))
    }

    override fun hashCode(): Int {
        return Objects.hash(this.name, this.value, this.order)
    }
}
