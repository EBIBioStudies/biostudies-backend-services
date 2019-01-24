package ac.uk.ebi.biostd.persistence.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.ManyToMany

@Entity
class User(

    @Column
    var login: String,

    @Column
    var email: String,

    @Column
    var secret: String

) {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column
    var fullName: String? = null

    @Column
    var superuser: Boolean = false

    @Lob
    var passwordDigest: ByteArray = ByteArray(0)

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    val groups: MutableSet<UserGroup> = mutableSetOf()

    fun addGroup(userGroup: UserGroup) = groups.add(userGroup)
}
