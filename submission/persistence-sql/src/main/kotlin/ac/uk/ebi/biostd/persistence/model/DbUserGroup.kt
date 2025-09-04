package ac.uk.ebi.biostd.persistence.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "UserGroup")
class DbUserGroup(
    var name: String,
    var description: String? = null,
    var secret: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToMany(mappedBy = "groups")
    val users = mutableSetOf<DbUser>()
}
