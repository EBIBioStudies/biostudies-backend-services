package ac.uk.ebi.biostd.persistence.model

import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass

const val NO_TABLE_INDEX = -1

@MappedSuperclass
open class Attribute(

        @Column
        val name: String,

        @Column
        val value: String,

        @Column
        val order: Int,

        @Column(name = "ord")
        val reference: Boolean) {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column(name = "nameQualifierString")
    var nameQualifier: String? = null

    @Column(name = "valueQualifierString")
    var valueQualifier: String? = null
}
