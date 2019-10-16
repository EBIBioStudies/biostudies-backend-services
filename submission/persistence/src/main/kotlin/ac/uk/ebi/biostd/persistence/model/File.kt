package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import java.util.Objects.equals
import java.util.Objects.hash
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

    override fun compareTo(other: File) =
        Comparator.comparing(File::order).thenComparing(File::tableIndex).compare(this, other)

    override fun equals(other: Any?) = when {
        other !is File -> false
        this === other -> true
        else -> equals(id, other.id)
            .and(equals(name, other.name))
            .and(equals(order, other.order))
            .and(equals(tableIndex, other.tableIndex))
    }

    override fun hashCode() = hash(id, name, order, tableIndex)
}
