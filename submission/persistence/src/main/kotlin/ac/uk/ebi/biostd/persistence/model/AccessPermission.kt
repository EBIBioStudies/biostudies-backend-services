package ac.uk.ebi.biostd.persistence.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "AccessPermission")
class AccessPermission(
    @Id
    @GeneratedValue
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type")
    val accessType: AccessType,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne
    @JoinColumn(name = "access_tag_id")
    val accessTag: AccessTag
)

enum class AccessType {
    READ, SUBMIT, ATTACH, UPDATE, DELETE
}
