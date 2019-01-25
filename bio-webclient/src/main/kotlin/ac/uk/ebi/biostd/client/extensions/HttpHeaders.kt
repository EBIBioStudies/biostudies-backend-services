package ac.uk.ebi.biostd.client.extensions

import ebi.ac.uk.model.constants.SUB_TYPE_HEADER
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

fun HttpHeaders.setSubmissionType(value: MediaType) = set(SUB_TYPE_HEADER, value.toString())
