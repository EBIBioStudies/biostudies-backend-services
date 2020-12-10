package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.converters.EpochDateConverter
import ac.uk.ebi.biostd.persistence.converters.NullableEpochDateConverter
import ac.uk.ebi.biostd.persistence.converters.ProcessingStatusConverter
import ac.uk.ebi.biostd.persistence.model.constants.ACC_TAGS
import ac.uk.ebi.biostd.persistence.model.constants.ATTRS
import ac.uk.ebi.biostd.persistence.model.constants.FILES
import ac.uk.ebi.biostd.persistence.model.constants.LINKS
import ac.uk.ebi.biostd.persistence.model.constants.SECTS
import ac.uk.ebi.biostd.persistence.model.constants.SUBMITTER
import ac.uk.ebi.biostd.persistence.model.constants.SUB_OWNER
import ac.uk.ebi.biostd.persistence.model.constants.SUB_ROOT_SECTION
import ac.uk.ebi.biostd.persistence.model.constants.TAGS
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
import javax.persistence.FetchType.LAZY
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
import javax.persistence.OrderBy
import javax.persistence.Table
import ac.uk.ebi.biostd.persistence.model.DbSection as SectionDb

typealias Node = NamedAttributeNode
typealias Graph = NamedSubgraph

internal const val SIMPLE_QUERY_GRAPH = "Submission.simpleGraph"
internal const val SUBMISSION_FULL_GRAPH = "Submission.simpleFullGraph"
internal const val SUBMISSION_AND_ROOT_SECTION_FULL_GRAPH = "Submission.fullGraph"

private const val ROOT_SECTION_GRAPH = "Submission.rootSectionGraph"
private const val ATTRIBUTES_GRAPH = "SubmissionObject.attributesGraph"

@Entity
@NamedEntityGraphs(value = [
    NamedEntityGraph(name = SIMPLE_QUERY_GRAPH, attributeNodes = [Node(value = SUB_ROOT_SECTION)]),
    NamedEntityGraph(name = SUBMISSION_FULL_GRAPH, attributeNodes = [
        Node(ATTRS),
        Node(ACC_TAGS),
        Node(TAGS),
        Node(SUB_OWNER),
        Node(SUBMITTER)
    ]),
    NamedEntityGraph(name = SUBMISSION_AND_ROOT_SECTION_FULL_GRAPH, attributeNodes = [
        Node(ATTRS),
        Node(ACC_TAGS),
        Node(TAGS),
        Node(SUB_OWNER),
        Node(SUBMITTER),
        Node(value = SUB_ROOT_SECTION, subgraph = ROOT_SECTION_GRAPH)
    ], subgraphs = [
        Graph(name = ROOT_SECTION_GRAPH, attributeNodes = [
            Node(LINKS, subgraph = ATTRIBUTES_GRAPH),
            Node(ATTRS),
            Node(SECTS),
            Node(FILES, subgraph = ATTRIBUTES_GRAPH)]),
        Graph(name = ATTRIBUTES_GRAPH, attributeNodes = [Node(ATTRS)])
    ])
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

    @ManyToOne(cascade = [CascadeType.ALL], fetch = LAZY)
    @JoinColumn(name = "rootSection_id")
    lateinit var rootSection: SectionDb

    @Column(name = "rootSection_id", updatable = false, insertable = false)
    var rootSectionId: Long = -1

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "owner_id")
    lateinit var owner: DbUser

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "submitter_id")
    lateinit var submitter: DbUser

    @ManyToMany
    @JoinTable(name = "Submission_AccessTag",
        joinColumns = [JoinColumn(name = "Submission_Id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "accessTags_id", referencedColumnName = "id")])
    var accessTags: MutableSet<DbAccessTag> = sortedSetOf()

    @ManyToMany
    @JoinTable(name = "Submission_ElementTag",
        joinColumns = [JoinColumn(name = "submission_Id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id", referencedColumnName = "id")])
    var tags: MutableSet<DbTag> = sortedSetOf()

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "submission_id")
    @OrderBy("order ASC")
    var attributes: SortedSet<DbSubmissionAttribute> = sortedSetOf()
}
