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
@Table(name = "Link")
class Link(
        @Column
        val url: String,

        @OneToMany
        @JoinColumn(name = "link_id")
        @OrderBy("order ASC")
        val attributes: MutableSet<Attribute>,

        @Column(name = "ord")
        override var order: Int) : Tabular, Comparable<Link> {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column
    override var tableIndex = NO_TABLE_INDEX

    override fun compareTo(other: Link) = this.order.compareTo(other.order)
}
