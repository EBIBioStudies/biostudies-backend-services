package uk.ac.ebi.biostd.client.cli.dto

import ebi.ac.uk.extended.model.FileMode

internal data class MigrationRequest(
    val accNo: String,
    val sourceSecurityConfig: SecurityConfig,
    val targetSecurityConfig: SecurityConfig,
    val targetOwner: String?,
    val fileMode: FileMode,
    val async: Boolean,
)
