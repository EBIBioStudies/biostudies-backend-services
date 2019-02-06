package ac.uk.ebi.biostd.persistence.model

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "UserGroup")
class UserGroup {

    @Id
    @GeneratedValue
    val id: Long = 0

    var name: String = ""
    var secret: String = ""

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], fetch = FetchType.EAGER)
    @JoinTable(name = "UserGroup_User",
        joinColumns = [JoinColumn(name = "groups_id")],
        inverseJoinColumns = [JoinColumn(name = "users_id")])
    val users = mutableSetOf<User>()
}
