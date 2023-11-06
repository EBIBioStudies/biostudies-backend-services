package ac.uk.ebi.biostd.stats.web

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ebi.ac.uk.model.SubmissionStat as SubmissionStatDto

fun SubmissionStat.toStatDto() = SubmissionStatDto(accNo, value, type.value)

fun SubmissionStatDto.toStat() = SingleSubmissionStat(accNo, value, SubmissionStatType.fromString(type))
