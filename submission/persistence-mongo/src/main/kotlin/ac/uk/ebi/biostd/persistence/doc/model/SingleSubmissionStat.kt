package ac.uk.ebi.biostd.persistence.doc.model

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType

class SingleSubmissionStat(
    override val accNo: String,
    override val value: Long,
    override val type: SubmissionStatType
) : SubmissionStat
