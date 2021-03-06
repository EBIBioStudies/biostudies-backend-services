package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.converters.NullableIntConverter
import ac.uk.ebi.biostd.persistence.model.constants.ATTRS
import ac.uk.ebi.biostd.persistence.model.constants.FILES
import ac.uk.ebi.biostd.persistence.model.constants.LINKS
import ac.uk.ebi.biostd.persistence.model.constants.SECTS
import java.util.Objects.equals
import java.util.Objects.hash
import java.util.SortedSet
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.FetchType.LAZY
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.NamedEntityGraph
import javax.persistence.NamedEntityGraphs
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.OrderBy
import javax.persistence.Table

internal const val SECTION_SIMPLE_GRAPH = "Section.simpleGraph"
private const val ATTRIBUTES_GRAPH = "Object.attributesGraph"

@Entity
@Table(name = "Section")
@NamedEntityGraphs(
    value = [
        NamedEntityGraph(
            name = SECTION_SIMPLE_GRAPH,
            attributeNodes = [
                Node(ATTRS),
                Node(SECTS),
                Node(LINKS, subgraph = ATTRIBUTES_GRAPH),
                Node(FILES, subgraph = ATTRIBUTES_GRAPH)
            ],
            subgraphs = [
                Graph(name = ATTRIBUTES_GRAPH, attributeNodes = [Node(ATTRS)])
            ]
        )
    ]
)
class DbSection(
    @Column
    var accNo: String?,

    @Column
    var type: String

) : Tabular, Comparable<DbSection> {
    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column
    override var tableIndex: Int = NO_TABLE_INDEX

    @Column(name = "ord")
    @Convert(converter = NullableIntConverter::class)
    override var order: Int = 0

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "submission_id")
    var submission: DbSubmission? = null

    @OneToOne(cascade = [CascadeType.ALL], fetch = LAZY)
    @JoinColumn(name = "fileListId")
    var fileList: ReferencedFileList? = null

    @OneToMany(cascade = [CascadeType.ALL], fetch = LAZY)
    @JoinColumn(name = "section_id")
    @OrderBy("order ASC")
    var links: SortedSet<DbLink> = sortedSetOf()

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "section_id")
    @OrderBy("order ASC")
    var attributes: SortedSet<DbSectionAttribute> = sortedSetOf()

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "sectionId")
    @OrderBy("order ASC")
    var files: SortedSet<DbFile> = sortedSetOf()

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "parent_id")
    @OrderBy("order ASC")
    var sections: SortedSet<DbSection> = sortedSetOf()

    constructor(
        accNo: String,
        type: String,
        attributes: SortedSet<DbSectionAttribute>,
        files: SortedSet<DbFile>,
        links: SortedSet<DbLink>,
        tableIndex: Int = NO_TABLE_INDEX
    ) : this(accNo, type) {
        this.tableIndex = tableIndex
        this.attributes = attributes
        this.files = files
        this.links = links
    }

    override fun compareTo(other: DbSection) = order.compareTo(other.order)

    override fun equals(other: Any?) = when {
        (other !is DbSection) -> false
        (this === other) -> true
        else -> equals(id, other.id)
            .and(equals(accNo, other.accNo))
            .and(equals(type, other.type))
            .and(equals(order, other.order))
            .and(equals(tableIndex, other.tableIndex))
    }

    override fun hashCode(): Int {
        return hash(id, accNo, type, order, tableIndex)
    }
}
