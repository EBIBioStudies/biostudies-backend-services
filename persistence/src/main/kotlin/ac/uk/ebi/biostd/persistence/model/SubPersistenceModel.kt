package ac.uk.ebi.biostd.persistence.model

import javax.persistence.*

internal const val EMPTY = ""
internal const val FULL_DATA_GRAPH = "Submission.fullData"

typealias Node = NamedAttributeNode
typealias Graph = NamedSubgraph

@Entity
@NamedEntityGraph(
        name = FULL_DATA_GRAPH,
        attributeNodes = [
            Node(value = "rootSection", subgraph = "sectionGraph"),
            Node("accessTags"),
            Node("attributes")
        ],
        subgraphs = [
            Graph(name = "sectionGraph", attributeNodes = [Node("links"), Node("files")])
        ])
@Table(name = "Submission")
data class Submission(

        @Id
        @GeneratedValue
        var id: Long = 0L,

        @Column(name = "RTime")
        var releaseTime: Long,

        @Column(name = "CTime")
        var creationTime: Long,

        @Column(name = "MTime")
        var modificationTime: Long,

        @Column
        var accNo: String,

        @Column
        var relPath: String,

        @Column
        var released: Boolean = false,

        @Column
        var rootPath: String,

        @Column
        var title: String,

        @Column
        var version: Int,

        @Column
        var secretKey: String) {

    @OneToOne
    @JoinColumn(name = "rootSection_id")
    lateinit var rootSection: Section

    @ManyToMany
    @JoinTable(name = "Submission_AccessTag",
            joinColumns = [JoinColumn(name = "Submission_Id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "accessTags_id", referencedColumnName = "id")])
    lateinit var accessTags: MutableSet<AccessTag>

    @OneToMany
    @JoinColumn(name = "submission_id")
    lateinit var attributes: MutableSet<SubmissionAttribute>
}

@Entity
@Table(name = "AccessTag")
data class AccessTag(

        @Id
        @GeneratedValue
        var id: Long = 0L,

        @Column
        var description: String?,

        @Column
        var name: String
)

@Entity
@Table(name = "Section")
data class Section(

        @Id
        @GeneratedValue
        var id: Long = 0L,

        @Column
        var accNo: String = EMPTY,

        @Column(name = "ord")
        var order: Int?,

        @Column
        var tableIndex: Int?
) {
    @ManyToOne
    @JoinColumn(name = "parent_id")
    lateinit var parentSection: Section

    @OneToMany
    @JoinColumn(name = "section_id")
    lateinit var links: MutableSet<Link>

    @OneToMany
    @JoinColumn(name = "sectionId")
    lateinit var files: MutableSet<File>
}

@Entity
@Table(name = "Link")
data class Link(

        @Id
        @GeneratedValue
        var id: Long = 0L,

        @Column
        var tableIndex: Int?,

        @Column
        var url: String = EMPTY,

        @Column(name = "ord")
        var order: Int?) {

    @ManyToOne
    @JoinColumn(name = "section_id")
    lateinit var section: Section

    @OneToMany
    @JoinColumn(name = "link_id")
    lateinit var attributes: MutableSet<LinkAttribute>
}

@Entity
@Table(name = "FileRef")
data class File(
        @Id
        @GeneratedValue
        var id: Long = 0L,

        @Column
        var name: String = EMPTY,

        @Column
        var size: Int = 0,

        @Column
        var tableIndex: Int = -1,

        @Column(name = "ord")
        var order: Int = 0,

        @Column
        var path: String = EMPTY) {

    @OneToMany
    @JoinColumn(name = "link_id")
    lateinit var attributes: MutableSet<FileAttribute>
}
