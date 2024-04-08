package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.model.AccessTag
import java.util.Objects
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "AccessTag")
class DbAccessTag(
    @Id
    @GeneratedValue
    var id: Long = 0L,
    @Column
    override var name: String,
) : AccessTag {
    override fun equals(other: Any?): Boolean {
        if (other !is DbAccessTag) return false
        if (this === other) return true
        return Objects.equals(this.name, other.name)
    }

    override fun hashCode(): Int {
        return Objects.hash(this.name)
    }
}
