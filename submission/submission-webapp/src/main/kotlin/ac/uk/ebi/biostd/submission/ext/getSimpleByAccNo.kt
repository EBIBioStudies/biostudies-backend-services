package ac.uk.ebi.biostd.submission.ext

import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission

fun SubmissionQueryService.getSimpleByAccNo(accNo: String) = getExtByAccNo(accNo).toSimpleSubmission()
