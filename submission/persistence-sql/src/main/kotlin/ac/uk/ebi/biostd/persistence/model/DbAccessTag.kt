package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.model.AccessTag
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.Objects

@Entity
@Table(name = "AccessTag")
class DbAccessTag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,
    @Column
    override var name: String,
) : AccessTag {
    override fun equals(other: Any?): Boolean {
        if (other !is DbAccessTag) return false
        if (this === other) return true
        return Objects.equals(this.name, other.name)
    }

    override fun hashCode(): Int = Objects.hash(this.name)
}
