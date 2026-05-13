package ebi.ac.uk.api.security

import io.swagger.v3.oas.annotations.media.Schema

class RetryActivationRequest(
    @field:Schema(description = "User email", example = "user@test.org", required = true)
    val email: String,
    @field:Schema(description = "Instance key to generate the activation link", example = "the-instance-key")
    val instanceKey: String,
    @field:Schema(description = "Path to redirect the user in the UI", example = "/users/register", required = true)
    val path: String,
)
