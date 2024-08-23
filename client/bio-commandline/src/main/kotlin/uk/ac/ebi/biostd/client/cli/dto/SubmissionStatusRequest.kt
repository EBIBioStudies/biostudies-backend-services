package uk.ac.ebi.biostd.client.cli.dto

internal class SubmissionStatusRequest(
    val accNo: String,
    val version: Int,
    val securityConfig: SecurityConfig,
)
