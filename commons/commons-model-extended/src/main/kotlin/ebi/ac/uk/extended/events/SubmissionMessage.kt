package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import ebi.ac.uk.util.date.asIsoTime
import java.io.Serializable
import java.time.OffsetDateTime

@Suppress("SerialVersionUIDInSerializableClass")
class SubmissionMessage(
    @JsonProperty("accNo")
    val accNo: String,
    @JsonProperty("pagetabUrl")
    val pagetabUrl: String,
    @JsonProperty("extTabUrl")
    val extTabUrl: String,
    @JsonProperty("extUserUrl")
    val extUserUrl: String,
    @JsonProperty("eventTime")
    val eventTime: String,
) : Serializable {
    companion object {
        fun createNew(
            accNo: String,
            owner: String,
            instanceBaseUrl: String,
        ): SubmissionMessage =
            SubmissionMessage(
                accNo = accNo,
                pagetabUrl = "$instanceBaseUrl/submissions/$accNo.json",
                extTabUrl = "$instanceBaseUrl/submissions/extended/$accNo",
                extUserUrl = "$instanceBaseUrl/security/users/extended/$owner",
                eventTime = OffsetDateTime.now().asIsoTime(),
            )
    }
}
