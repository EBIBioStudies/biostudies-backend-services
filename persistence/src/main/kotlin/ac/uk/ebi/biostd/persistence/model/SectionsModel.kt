package ac.uk.ebi.biostd.persistence.model

import ebi.ac.uk.base.EMPTY
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.MappedSuperclass
import javax.persistence.OneToMany
import javax.persistence.OrderBy
import javax.persistence.Table

@MappedSuperclass
abstract class AbstractSection {

    @Column
    var accNo: String? = null

    @Column
    var type: String = EMPTY

    @OneToMany
    @JoinColumn(name = "section_id")
    @OrderBy("order ASC")
    lateinit var attributes: MutableSet<SectionAttribute>

    @OneToMany
    @JoinColumn(name = "section_id")
    lateinit var links: MutableSet<Link>

    @OneToMany
    @JoinColumn(name = "sectionId")
    lateinit var files: MutableSet<File>

    @OneToMany
    @JoinColumn(name = "parent_id")
    lateinit var sections: MutableSet<Section>
}

@Entity
@Table(name = "Section")
data class RootSection(
        @Id
        @GeneratedValue
        var id: Long = 0L) : AbstractSection()

@Entity
@Table(name = "Section")
data class Section(
        @Id
        @GeneratedValue
        var id: Long = 0L) : AbstractSection() {

    @Column
    var tableIndex: Int = NO_TABLE_INDEX

    @Column(name = "ord")
    var order: Int = 0
}
