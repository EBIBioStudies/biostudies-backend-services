package ac.uk.ebi.biostd.persistence.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Lob

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
}
