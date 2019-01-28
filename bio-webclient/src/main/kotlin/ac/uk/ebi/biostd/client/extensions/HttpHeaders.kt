package ac.uk.ebi.biostd.client.extensions

import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

fun HttpHeaders.setSubmissionType(value: MediaType) = set(SUBMISSION_TYPE, value.toString())
