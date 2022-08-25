package ac.uk.ebi.biostd.stats.web.mapping

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ac.uk.ebi.biostd.stats.web.model.SubmissionStatDto

// TODO add tests
fun SubmissionStat.toStatDto() = SubmissionStatDto(accNo, value, type.value)

fun SubmissionStatDto.toStat() = SingleSubmissionStat(accNo, value, SubmissionStatType.fromString(type))
