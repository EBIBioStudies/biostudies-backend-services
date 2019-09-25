package ac.uk.ebi.biostd.persistence.model

import java.util.SortedSet
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OrderBy
import javax.persistence.Table

@Entity
@Table(name = "FileList")
class ReferencedFileList(
    @Column
    var name: String
) {
    @Id
    @GeneratedValue
    var id: Long = 0L

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "fileListId")
    @OrderBy("order ASC")
    @Basic(fetch = FetchType.LAZY)
    var files: SortedSet<ReferencedFile> = sortedSetOf()

    constructor(name: String, files: SortedSet<ReferencedFile>) : this(name) {
        this.files = files
    }
}

@Entity
@Table(name = "ReferencedFile")
class ReferencedFile(
    @Column
    val name: String,

    @Column(name = "ord")
    override var order: Int

) : Sortable, Comparable<ReferencedFile> {
    @Id
    @GeneratedValue
    var id: Long = 0L

    var size: Long = 0L

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "referenced_file_id")
    @OrderBy("order ASC")
    var attributes: SortedSet<ReferencedFileAttribute> = sortedSetOf()

    constructor(name: String, order: Int, size: Long, attributes: SortedSet<ReferencedFileAttribute>) :
        this(name, order) {
        this.size = size
        this.attributes = attributes
    }

    override fun compareTo(other: ReferencedFile) = this.order.compareTo(other.order)
}
