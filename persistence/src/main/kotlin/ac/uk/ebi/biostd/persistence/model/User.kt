package ac.uk.ebi.biostd.persistence.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class User {

    @Id
    @GeneratedValue
    var id: Long = 0L

    @Column
    lateinit var email: String

    @Column
    lateinit var secret: String

    @Column
    lateinit var fullName: String

    @Column
    lateinit var login: String

    @Column
    var superuser: Boolean = false
}
