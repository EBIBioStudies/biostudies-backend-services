package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import java.util.Objects
import java.util.SortedSet
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

    @Column(name = "ord")
    override var order: Int
) : Tabular, Comparable<Link> {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @OneToMany
    @JoinColumn(name = "link_id")
    @OrderBy("order ASC")
    var attributes: SortedSet<LinkAttribute> = sortedSetOf()

    @Column
    override var tableIndex = NO_TABLE_INDEX

    constructor(url: String, order: Int, attributes: SortedSet<LinkAttribute>) : this(url, order) {
        this.attributes = attributes
    }

    override fun compareTo(other: Link) = this.order.compareTo(other.order)

    override fun equals(other: Any?): Boolean {
        if (other !is Link) return false
        if (this === other) return true

        return Objects.equals(this.url, other.url).and(Objects.equals(this.order, this.order))
    }

    override fun hashCode(): Int {
        return Objects.hash(this.url, this.order)
    }
}
