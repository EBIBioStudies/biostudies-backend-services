package ac.uk.ebi.biostd.persistence.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class User(

        @Id
        @GeneratedValue
        var id: Long = 0L,

        var email: String
)
