package ebi.ac.uk.notifications.persistence.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "SubmissionRT")
class SubmissionRT(
    @Column
    val accNo: String,

    @Column
    val ticketId: String
) {
    @Id
    @GeneratedValue
    var id: Long = 0L
}
