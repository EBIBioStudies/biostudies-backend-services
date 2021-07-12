package uk.ac.ebi.biostd.client.cli.dto

internal data class PermissionRequest(
    val server: String,
    val user: String,
    val password: String,
    val accessType: String,
    val targetUser: String,
    val accessTagName: String
)
