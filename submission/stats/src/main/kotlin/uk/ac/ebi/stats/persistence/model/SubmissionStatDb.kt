package uk.ac.ebi.stats.persistence.model

import uk.ac.ebi.stats.model.SubmissionStatType
import uk.ac.ebi.stats.model.SubmissionStatType.UNKNOWN
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "SubmissionStat")
class SubmissionStatDb(
    @Column
    val accNo: String,

    @Column
    val value: Long,

    @Column
    @Enumerated(EnumType.STRING)
    val type: SubmissionStatType
) {
    @Id
    @GeneratedValue
    var id: Long = 0L

    constructor() : this("", -1, UNKNOWN)
}
