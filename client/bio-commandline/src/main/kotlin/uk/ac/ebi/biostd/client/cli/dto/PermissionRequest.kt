package uk.ac.ebi.biostd.client.cli.dto

internal data class PermissionRequest(
    val securityConfig: SecurityConfig,
    val accessType: String,
    val targetUser: String,
    val accessTagName: String,
)
