package uk.ac.ebi.biostd.client.cli.dto

data class DeletionRequest(
    val server: String,
    val user: String,
    val password: String,
    val onBehalf: String?,
    val accNo: String
)
