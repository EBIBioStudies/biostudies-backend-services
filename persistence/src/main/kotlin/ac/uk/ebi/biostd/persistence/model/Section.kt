package ac.uk.ebi.biostd.persistence.model

import ebi.ac.uk.base.EMPTY
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OrderBy
import javax.persistence.Table

@Entity
@Table(name = "Section")
class Section : Tabular, Comparable<Section> {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column
    var accNo: String? = null

    @Column
    var type: String = EMPTY

    @Column
    override var tableIndex: Int = NO_TABLE_INDEX

    @Column(name = "ord")
    override var order: Int = 0

    override fun compareTo(other: Section) = order.compareTo(other.order)

    @OneToMany
    @JoinColumn(name = "section_id")
    @OrderBy("order ASC")
    lateinit var attributes: SortedSet<Attribute>

    @OneToMany
    @JoinColumn(name = "section_id")
    lateinit var links: SortedSet<Link>

    @OneToMany
    @JoinColumn(name = "sectionId")
    lateinit var files: SortedSet<File>

    @OneToMany
    @JoinColumn(name = "parent_id")
    lateinit var sections: SortedSet<Section>
}