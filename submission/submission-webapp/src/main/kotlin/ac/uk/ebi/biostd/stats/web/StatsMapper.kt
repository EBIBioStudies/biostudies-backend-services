package ac.uk.ebi.biostd.stats.web

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import uk.ac.ebi.biostd.model.SubmissionStat as SubmissionStatDto

fun SubmissionStat.toStatDto() = SubmissionStatDto(accNo, value, type.value)

fun SubmissionStatDto.toStat() = SubmissionStat(accNo, value, SubmissionStatType.fromString(type))
