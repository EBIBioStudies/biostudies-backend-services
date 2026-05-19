package ebi.ac.uk.api.security

import io.swagger.v3.oas.annotations.media.Schema

class LoginRequest(
    @field:Schema(description = "User login or email", example = "user@test.org", required = true)
    val login: String,
    @field:Schema(description = "User password", example = "123456", required = true)
    val password: String,
)
