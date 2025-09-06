package ac.uk.ebi.biostd.persistence.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.Objects

@Entity
@Table(name = "ElementTag", uniqueConstraints = [UniqueConstraint(columnNames = ["name", "classifier"])])
class DbTag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column
    var classifier: String,
    @Column
    var name: String,
) {
    override fun equals(other: Any?) =
        when {
            other !is DbTag -> false
            other === this -> true
            else ->
                Objects
                    .equals(id, other.id)
                    .and(Objects.equals(name, other.name))
                    .and(Objects.equals(classifier, other.classifier))
        }

    override fun hashCode() = Objects.hash(id, name, classifier)
}
