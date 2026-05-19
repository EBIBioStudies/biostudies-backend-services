package ebi.ac.uk.api.security

import io.swagger.v3.oas.annotations.media.Schema

data class ActivateByEmailRequest(
    @field:Schema(description = "User email", example = "user@test.org", required = true)
    val email: String,
    @field:Schema(
        description = "Instance key that should process the password reset",
        example = "the-instance-key",
        required = true,
    )
    val instanceKey: String,
    @field:Schema(
        description = "Path to redirect the user in the UI",
        example = "/users/password_setup",
        required = true,
    )
    val path: String,
)
