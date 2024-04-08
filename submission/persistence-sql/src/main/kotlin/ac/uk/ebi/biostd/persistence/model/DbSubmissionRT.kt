package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRT
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "SubmissionRT")
class DbSubmissionRT(
    @Column
    override val accNo: String,
    @Column
    override val ticketId: String,
) : SubmissionRT {
    @Id
    @GeneratedValue
    var id: Long = 0L

    constructor() : this("", "")
}
