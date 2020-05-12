package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.converters.EpochDateConverter
import ac.uk.ebi.biostd.persistence.converters.NullableEpochDateConverter
import ac.uk.ebi.biostd.persistence.converters.ProcessingStatusConverter
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.ProcessingStatus
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSING
import java.time.OffsetDateTime
import java.util.SortedSet
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph
import javax.persistence.NamedEntityGraphs
import javax.persistence.NamedSubgraph
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.OrderBy
import javax.persistence.Table
import ac.uk.ebi.biostd.persistence.model.DbSection as SectionDb

internal const val FULL_DATA_GRAPH = "Submission.fullData"
internal const val SIMPLE_QUERY_GRAPH = "Submission.simpleGraph"

internal const val ATTRS = "attributes"
internal const val FILES = "files"
internal const val LINKS = "links"
internal const val SECTS = "sections"

typealias Node = NamedAttributeNode
typealias Graph = NamedSubgraph

@Entity
@NamedEntityGraphs(value = [
    NamedEntityGraph(name = FULL_DATA_GRAPH,
        attributeNodes = [
            Node(value = "rootSection", subgraph = "root"),
            Node("accessTags"),
            Node("tags"),
            Node(ATTRS),
            Node("owner")
        ],
        subgraphs = [
            Graph(name = "root", attributeNodes = [
                Node(LINKS, subgraph = "attrs"),
                Node(ATTRS),
                Node(FILES, subgraph = "attrs"),
                Node(SECTS, subgraph = "l1")]),
            Graph(name = "l1", attributeNodes = [
                Node(LINKS, subgraph = "attrs"),
                Node(ATTRS), Node(FILES, subgraph = "attrs"),
                Node(SECTS, subgraph = "l2")]),
            Graph(name = "l2", attributeNodes = [
                Node(LINKS, subgraph = "attrs"),
                Node(ATTRS),
                Node(FILES, subgraph = "attrs"),
                Node(SECTS, subgraph = "l3")]),
            Graph(name = "l3", attributeNodes = [
                Node(LINKS, subgraph = "attrs"),
                Node(ATTRS),
                Node(FILES, subgraph = "attrs")]),
            Graph(name = "attrs", attributeNodes = [Node(ATTRS)])
        ]),
    NamedEntityGraph(name = SIMPLE_QUERY_GRAPH, attributeNodes = [Node(value = "rootSection")])
])
@Table(name = "Submission")
class DbSubmission(

    @Column
    var accNo: String = "",

    @Column
    var version: Int = 1

) {
    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column(name = "RTime")
    @Convert(converter = NullableEpochDateConverter::class)
    var releaseTime: OffsetDateTime? = null

    @Column(name = "CTime")
    @Convert(converter = EpochDateConverter::class)
    lateinit var creationTime: OffsetDateTime

    @Column(name = "MTime")
    @Convert(converter = EpochDateConverter::class)
    lateinit var modificationTime: OffsetDateTime

    @Column
    var relPath: String = ""

    @Column
    var released: Boolean = false

    @Column
    var rootPath: String? = null

    @Column
    @Enumerated(EnumType.STRING)
    var method: SubmissionMethod? = null

    @Column
    var title: String? = null

    @Column
    var secretKey: String = ""

    @Column
    @Convert(converter = ProcessingStatusConverter::class)
    var status: ProcessingStatus = PROCESSING

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "rootSection_id")
    lateinit var rootSection: SectionDb

    @ManyToOne
    @JoinColumn(name = "owner_id")
    lateinit var owner: DbUser

    @ManyToOne
    @JoinColumn(name = "submitter_id")
    lateinit var submitter: DbUser

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(name = "Submission_AccessTag",
        joinColumns = [JoinColumn(name = "Submission_Id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "accessTags_id", referencedColumnName = "id")])
    var accessTags: MutableSet<DbAccessTag> = sortedSetOf()

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(name = "Submission_ElementTag",
        joinColumns = [JoinColumn(name = "submission_Id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id", referencedColumnName = "id")])
    var tags: MutableSet<DbTag> = sortedSetOf()

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "submission_id")
    @OrderBy("order ASC")
    var attributes: SortedSet<DbSubmissionAttribute> = sortedSetOf()
}
