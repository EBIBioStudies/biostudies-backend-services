package ac.uk.ebi.biostd.persistence.model

import java.util.Objects
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "Tag")
class Tag(
    @Id
    @GeneratedValue
    var id: Long = 0,

    @Column(unique = true)
    var name: String
) {
    @ManyToOne
    @JoinColumn(name = "classifier_id")
    var classifier: Classifier? = null

    @ManyToOne
    @JoinColumn(name = "parent_tag_id")
    var parentTag: Tag? = null

    override fun equals(other: Any?) = when {
        other !is Tag -> false
        other === this -> true
        else -> Objects.equals(id, other.id)
            .and(Objects.equals(name, other.name))
            .and(Objects.equals(parentTag, other.parentTag))
            .and(Objects.equals(classifier, other.classifier))
    }

    override fun hashCode() = Objects.hash(id, name, classifier, parentTag)
}

@Entity
@Table(name = "Classifier")
class Classifier(
    @Id
    @GeneratedValue
    var id: Int = 0,

    @Column(unique = true)
    var name: String
) {
    override fun equals(other: Any?) = when {
        other !is Classifier -> false
        other === this -> true
        else -> Objects.equals(id, other.id)
            .and(Objects.equals(name, other.name))
    }

    override fun hashCode() = Objects.hash(id, name)
}
