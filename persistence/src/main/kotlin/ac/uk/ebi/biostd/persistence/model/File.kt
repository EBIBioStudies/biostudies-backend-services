package ac.uk.ebi.biostd.persistence.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OrderBy
import javax.persistence.Table

@Entity
@Table(name = "FileRef")
class File(

        @Column
        val name: String,

        @OneToMany
        @JoinColumn(name = "file_id")
        @OrderBy("order ASC")
        val attributes: MutableSet<Attribute>,

        @Column(name = "ord")
        override var order: Int) : Tabular, Comparable<File> {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column
    override var tableIndex = NO_TABLE_INDEX

    override fun compareTo(other: File) = this.order.compareTo(other.order)
}