package ac.uk.ebi.biostd.persistence.model

import java.util.Objects
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(name = "ElementTag", uniqueConstraints = [ UniqueConstraint(columnNames = ["name", "classifier" ]) ])
class Tag(
    @Id
    @GeneratedValue
    var id: Long = 0,

    @Column
    var classifier: String,

    @Column
    var name: String
) {
    override fun equals(other: Any?) = when {
        other !is Tag -> false
        other === this -> true
        else -> Objects.equals(id, other.id)
            .and(Objects.equals(name, other.name))
            .and(Objects.equals(classifier, other.classifier))
    }

    override fun hashCode() = Objects.hash(id, name, classifier)
}
