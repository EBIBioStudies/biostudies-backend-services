package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.model.TransferLog
import ac.uk.ebi.biostd.persistence.common.model.TransferOperation
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "TransferLog")
class DbTransferLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    val timestamp: Instant,
    @Column(nullable = false)
    val user: String,
    @Column(nullable = false)
    val sourceEmail: String,
    @Column(nullable = false)
    val targetEmail: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val operation: TransferOperation,
) {
    fun asTransferLog() = TransferLog(timestamp, user, sourceEmail, targetEmail, operation)
}
