package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.converters.NullableIntConverter
import java.util.Objects
import java.util.SortedSet
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
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

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "libraryFile")
    var libraryFile: LibraryFile? = null

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "section_id")
    @OrderBy("order ASC")
    var attributes: SortedSet<SectionAttribute> = sortedSetOf()

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "section_id")
    @OrderBy("order ASC")
    var links: SortedSet<Link> = sortedSetOf()

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "sectionId")
    @OrderBy("order ASC")
    var files: SortedSet<File> = sortedSetOf()

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "parent_id")
    @OrderBy("order ASC")
    var sections: SortedSet<Section> = sortedSetOf()

    override fun compareTo(other: Section) = order.compareTo(other.order)

    override fun equals(other: Any?) = when {
        (other !is Section) -> false
        (this === other) -> true
        else -> Objects.equals(this.accNo, other.accNo)
    }

    override fun hashCode(): Int {
        return Objects.hash(this.accNo, this.accNo)
    }
}
