package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import java.util.Objects.equals
import java.util.Objects.hash
import java.util.SortedSet
import javax.persistence.CascadeType
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
class DbLink(
    @Column
    val url: String,

    @Column(name = "ord")
    override var order: Int
) : Tabular, Comparable<DbLink> {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "link_id")
    @OrderBy("order ASC")
    var attributes: SortedSet<DbLinkAttribute> = sortedSetOf()

    @Column
    override var tableIndex = NO_TABLE_INDEX

    constructor(url: String, order: Int, attributes: SortedSet<DbLinkAttribute>, tableIndex: Int = NO_TABLE_INDEX) :
        this(url, order) {
            this.attributes = attributes
            this.tableIndex = tableIndex
        }

    override fun compareTo(other: DbLink) = this.order.compareTo(other.order)

    override fun equals(other: Any?): Boolean = when {
        other !is DbLink -> false
        this === other -> true
        else -> equals(id, other.id)
            .and(equals(url, other.url))
            .and(equals(order, other.order))
            .and(equals(tableIndex, other.tableIndex))
    }

    override fun hashCode(): Int = hash(id, url, order, tableIndex)
}
