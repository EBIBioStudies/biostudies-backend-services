package ac.uk.ebi.biostd.persistence.model

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "SubmissionStat")
class DbSubmissionStat(
    @Column
    override val accNo: String,

    @Column
    override val value: Long,

    @Column
    @Enumerated(EnumType.STRING)
    override val type: SubmissionStatType
) : SubmissionStat {
    @Id
    @GeneratedValue
    var id: Long = 0L

    constructor() : this("", -1, VIEWS)
}
