package uk.ac.ebi.biostd.client.cli.dto

import ebi.ac.uk.extended.model.FileMode

internal data class MigrationRequest(
    val accNo: String,
    val source: String,
    val sourceUser: String,
    val sourcePassword: String,
    val target: String,
    val targetUser: String,
    val targetPassword: String,
    val targetOwner: String?,
    val tempFolder: String,
    val fileMode: FileMode,
    val async: Boolean
)
