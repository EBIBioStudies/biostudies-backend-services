package uk.ac.ebi.biostd.client.cli.dto

import ebi.ac.uk.extended.model.StorageMode

internal data class TransferRequest(
    val accNo: String,
    val target: StorageMode,
    val securityConfig: SecurityConfig,
)
