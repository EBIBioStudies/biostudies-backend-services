package ac.uk.ebi.biostd.persistence.model

import java.io.Serializable
import javax.persistence.*

@MappedSuperclass
abstract class Attribute {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Id
    @Column
    var name: String = EMPTY

    @Column(name = "nameQualifierString")
    var nameQualifier: String? = null

    @Column
    var reference: Boolean? = null

    @Column
    var value: String = EMPTY

    @Column(name = "valueQualifierString")
    var valueQualifier: String? = null

    @Column(name = "ord")
    var order: Int = 0
}

@Entity
data class SubmissionAttribute(

        @ManyToOne
        @JoinColumn(name = "submission_id")
        var submission: Submission

) : Attribute(), Serializable

@Entity
data class SectionAttribute(

        @ManyToOne
        @JoinColumn(name = "section_id")
        var section: Section

) : Attribute(), Serializable

@Entity
data class LinkAttribute(

        @ManyToOne
        @JoinColumn(name = "link_id")
        var link: Link

) : Attribute(), Serializable

@Entity
data class FileAttribute(

        @ManyToOne
        @JoinColumn(name = "file_id")
        var file: File

) : Attribute(), Serializable