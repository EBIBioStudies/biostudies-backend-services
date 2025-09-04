package ac.uk.ebi.biostd.security.web.exception

import ac.uk.ebi.biostd.security.web.dto.SecurityError
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.commons.http.servlet.write
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

private const val AUTHORIZATION_ERROR = "Problems authorizing user."

/**
 * Handle spring security authentication exceptions e.g. try to access authenticated resources without authentication
 * credentials.
 *
 */
class SecurityAuthEntryPoint(
    private val mapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        excep: AuthenticationException,
    ) {
        response.write(UNAUTHORIZED) { mapper.writeValue(it, SecurityError(excep.message ?: AUTHORIZATION_ERROR)) }
    }
}
