package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import java.util.*
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
        override var order: Int) : Tabular, Comparable<File> {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @OneToMany
    @JoinColumn(name = "file_id")
    @OrderBy("order ASC")
    var attributes: SortedSet<FileAttribute> = sortedSetOf()

    constructor(name: String, order: Int, attributes: SortedSet<FileAttribute>) : this(name, order) {
        this.attributes = attributes
    }

    @Column
    override var tableIndex = NO_TABLE_INDEX

    override fun compareTo(other: File) = this.order.compareTo(other.order)
}