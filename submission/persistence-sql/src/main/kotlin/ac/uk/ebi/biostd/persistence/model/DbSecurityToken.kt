package ac.uk.ebi.biostd.persistence.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "SecurityToken")
class DbSecurityToken(
    @Id
    var id: String,
    @Column(name = "invalidation_date")
    val invalidationDate: OffsetDateTime,
)
