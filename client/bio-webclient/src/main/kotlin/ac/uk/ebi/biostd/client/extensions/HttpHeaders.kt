package ac.uk.ebi.biostd.client.extensions

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import uk.ac.ebi.biostd.model.constants.SUBMISSION_TYPE

fun HttpHeaders.setSubmissionType(value: MediaType) = set(SUBMISSION_TYPE, value.toString())
