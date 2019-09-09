package ac.uk.ebi.biostd.persistence.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "UserGroup")
class UserGroup(
    var name: String,
    var description: String,
    var secret: String
) {

    @Id
    @GeneratedValue
    val id: Long = 0

    @ManyToMany(mappedBy = "groups")
    val users = mutableSetOf<User>()
}
