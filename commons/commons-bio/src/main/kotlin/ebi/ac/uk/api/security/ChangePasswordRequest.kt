package ebi.ac.uk.api.security

import io.swagger.v3.oas.annotations.media.Schema

class ChangePasswordRequest(
    @field:Schema(description = "User activation key", example = "the-activation-key", required = true)
    val activationKey: String,
    @field:Schema(description = "New user password", example = "7891011", required = true)
    val password: String,
)
