package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.converters.NullableIntConverter
import java.util.Objects
import java.util.SortedSet
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OrderBy
import javax.persistence.Table

@Entity
@Table(name = "Section")
class Section(

    @Column
    var accNo: String?,

    @Column
    var type: String

) : Tabular, Comparable<Section> {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column
    override var tableIndex: Int = NO_TABLE_INDEX

    @Column(name = "ord")
    @Convert(converter = NullableIntConverter::class)
    override var order: Int = 0

    @OneToMany
    @JoinColumn(name = "section_id")
    @OrderBy("order ASC")
    var attributes: SortedSet<SectionAttribute> = sortedSetOf()

    @OneToMany
    @JoinColumn(name = "section_id")
    @OrderBy("order ASC")
    var links: SortedSet<Link> = sortedSetOf()

    @OneToMany
    @JoinColumn(name = "sectionId")
    @OrderBy("order ASC")
    var files: SortedSet<File> = sortedSetOf()

    @OneToMany
    @JoinColumn(name = "parent_id")
    @OrderBy("order ASC")
    var sections: SortedSet<Section> = sortedSetOf()

    override fun compareTo(other: Section) = order.compareTo(other.order)

    override fun equals(other: Any?): Boolean {
        if (other !is Section) return false
        if (this === other) return true

        return Objects.equals(this.accNo, other.accNo)
    }

    override fun hashCode(): Int {
        return Objects.hash(this.accNo, this.accNo)
    }
}