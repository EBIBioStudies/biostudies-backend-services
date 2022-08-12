package uk.ac.ebi.biostd.client.cli.dto

internal data class MigrationRequest(
    val accNo: String,
    val sourceSecurityConfig: SecurityConfig,
    val targetSecurityConfig: SecurityConfig,
    val targetOwner: String?,
    val async: Boolean,
)
