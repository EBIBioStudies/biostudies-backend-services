package ebi.ac.uk.api.security

import io.swagger.v3.oas.annotations.media.Schema

class LogoutRequest(
    @field:Schema(description = "Authorization token to expire", example = "<user token>", required = true)
    val sessid: String,
)
