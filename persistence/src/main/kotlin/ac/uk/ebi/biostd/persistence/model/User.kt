package ac.uk.ebi.biostd.persistence.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class User(
        @Column
        var email: String) {

    @Id
    @GeneratedValue
    var id: Long = 0L
}
