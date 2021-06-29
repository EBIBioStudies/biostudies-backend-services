package uk.ac.ebi.biostd.client.cli.dto

internal data class MigrationRequest(
    val accNo: String,
    val source: String,
    val sourceUser: String,
    val sourcePassword: String,
    val target: String,
    val targetUser: String,
    val targetPassword: String,
    val targetOwner: String?
)
