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
class FileList(
    @Column
    var name: String
) {
    @Id
    @GeneratedValue
    var id: Long = 0L

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "fileListId")
    @Basic(fetch = FetchType.LAZY)
    var files: Set<ReferencedFile> = setOf()
}

@Entity
@Table(name = "ReferencedFile")
class ReferencedFile(
    @Column
    val name: String
) {
    @Id
    @GeneratedValue
    var id: Long = 0L

    var size: Long = 0L

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "referenced_file_id")
    @OrderBy("order ASC")
    var attributes: SortedSet<ReferencedFileAttribute> = sortedSetOf()

    constructor(name: String, size: Long, attributes: SortedSet<ReferencedFileAttribute>) : this(name) {
        this.size = size
        this.attributes = attributes
    }
}
