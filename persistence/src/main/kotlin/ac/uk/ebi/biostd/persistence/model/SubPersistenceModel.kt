package ac.uk.ebi.biostd.persistence.model

import ebi.ac.uk.base.EMPTY
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph
import javax.persistence.NamedSubgraph
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.OrderBy
import javax.persistence.Table

internal const val FULL_DATA_GRAPH = "Submission.fullData"
internal const val ATTRS = "attributes"
internal const val FILES = "files"
internal const val LINKS = "links"
internal const val SECTS = "sections"

typealias Node = NamedAttributeNode
typealias Graph = NamedSubgraph

@Entity
data class User(

        @Id
        @GeneratedValue
        var id: Long = 0L,

        var email: String
)

@Entity
@NamedEntityGraph(name = FULL_DATA_GRAPH,
        attributeNodes = [Node(value = "rootSection", subgraph = "root"), Node("accessTags"), Node(ATTRS), Node("owner")],
        subgraphs = [
            Graph(name = "root", attributeNodes = [Node(LINKS, subgraph = "attrs"), Node(ATTRS), Node(FILES, subgraph = "attrs"), Node(SECTS, subgraph = "l1")]),
            Graph(name = "l1", attributeNodes = [Node(LINKS, subgraph = "attrs"), Node(ATTRS), Node(FILES, subgraph = "attrs"), Node(SECTS, subgraph = "l2")]),
            Graph(name = "l2", attributeNodes = [Node(LINKS, subgraph = "attrs"), Node(ATTRS), Node(FILES, subgraph = "attrs"), Node(SECTS, subgraph = "l3")]),
            Graph(name = "l3", attributeNodes = [Node(LINKS, subgraph = "attrs"), Node(ATTRS), Node(FILES, subgraph = "attrs")]),
            Graph(name = "attrs", attributeNodes = [Node(ATTRS)])
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
        var rootPath: String?,

        @Column
        var title: String,

        @Column
        var version: Int,

        @Column
        var secretKey: String) {

    @OneToOne
    @JoinColumn(name = "rootSection_id")
    lateinit var rootSection: RootSection

    @ManyToOne
    @JoinColumn(name = "owner_id")
    lateinit var owner: User

    @ManyToMany
    @JoinTable(name = "Submission_AccessTag",
            joinColumns = [JoinColumn(name = "Submission_Id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "accessTags_id", referencedColumnName = "id")])
    lateinit var accessTags: MutableSet<AccessTag>

    @OneToMany
    @JoinColumn(name = "submission_id")
    @OrderBy("order ASC")
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
@Table(name = "Link")
data class Link(

        @Id
        @GeneratedValue
        var id: Long = 0L,

        @Column
        override var tableIndex: Int = -1,

        @Column
        var url: String = EMPTY,

        @Column(name = "ord")
        override var order: Int) : Tabular {

    @OneToMany
    @JoinColumn(name = "link_id")
    @OrderBy("order ASC")
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
        override var tableIndex: Int = -1,

        @Column(name = "ord")
        override var order: Int = 0,

        @Column
        var path: String = EMPTY) : Tabular {

    @OneToMany
    @JoinColumn(name = "file_id")
    @OrderBy("order ASC")
    lateinit var attributes: MutableSet<FileAttribute>
}

interface Tabular {

    var tableIndex: Int

    var order: Int
}
