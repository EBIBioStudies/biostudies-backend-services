package ac.uk.ebi.biostd.persistence.model

import java.time.OffsetDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class SecurityToken {

    @Id
    private lateinit var id: String

    @Column(name = "invalidation_date")
    private val invalidationDate: OffsetDateTime? = null
}
