package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRT
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "SubmissionRT")
class DbSubmissionRT(
    @Column
    override val accNo: String,
    @Column
    override val ticketId: String,
) : SubmissionRT {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    constructor() : this("", "")
}
