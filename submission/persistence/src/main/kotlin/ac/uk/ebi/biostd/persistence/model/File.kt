package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import java.util.SortedSet
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OrderBy
import javax.persistence.Table

@Entity
@Table(name = "FileRef")
class File(

    @Column
    val name: String,

    @Column(name = "ord")
    override var order: Int
) : Tabular, Comparable<File> {

    @Id
    @GeneratedValue
    var id: Long = 0L

    var size: Long = 0L

    @Transient
    lateinit var file: java.io.File

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "file_id")
    @OrderBy("order ASC")
    var attributes: SortedSet<FileAttribute> = sortedSetOf()

    constructor(
        name: String,
        order: Int,
        size: Long,
        attributes: SortedSet<FileAttribute>,
        tableIndex: Int = NO_TABLE_INDEX
    ) : this(name, order) {
        this.size = size
        this.attributes = attributes
        this.tableIndex = tableIndex
    }

    @Column
    override var tableIndex = NO_TABLE_INDEX

    override fun compareTo(other: File) = this.order.compareTo(other.order)
}
