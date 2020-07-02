package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

// TODO cambiar a llamar el servicio de extended y obtenerla
class SubmissionSubmitted(
    @JsonProperty("accNo")
    val accNo: String,

    @JsonProperty("version")
    val version: Int,

    @JsonProperty("title")
    val title: String,

    @JsonProperty("ownerEmail")
    val ownerEmail: String,

    @JsonProperty("ownerFullName")
    val ownerFullName: String,

    @JsonProperty("releaseDate")
    val releaseDate: String,

    @JsonProperty("released")
    val released: Boolean,

    @JsonProperty("secretKey")
    val secretKey: String,

    @JsonProperty("uiUrl")
    val uiUrl: String,

    @JsonProperty("pagetabUrl")
    val pagetabUrl: String,

    @JsonProperty("extTabUrl")
    val extTabUrl: String
) : Serializable
