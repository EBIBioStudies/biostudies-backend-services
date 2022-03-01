package ac.uk.ebi.biostd.persistence.model

import java.time.OffsetDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "SecurityToken")
class DbSecurityToken(

    @Id
    var id: String,

    @Column(name = "invalidation_date")
    val invalidationDate: OffsetDateTime
)
