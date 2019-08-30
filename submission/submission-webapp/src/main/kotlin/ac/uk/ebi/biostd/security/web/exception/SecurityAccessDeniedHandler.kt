package ac.uk.ebi.biostd.security.web.exception

import ac.uk.ebi.biostd.security.web.dto.SecurityError
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.commons.http.servlet.write
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private const val ACCESS_DENIED_ERROR = "Authenticated user does not have not access the given resource."

/**
 * Handle spring security denied access exception produced when an authorization issue e.g. user try to access a
 * resource which does not have access.
 */
class SecurityAccessDeniedHandler(private val mapper: ObjectMapper) : AccessDeniedHandler {

    override fun handle(request: HttpServletRequest, response: HttpServletResponse, exception: AccessDeniedException) {
        response.write(FORBIDDEN) { mapper.writeValue(it, SecurityError(exception.message ?: ACCESS_DENIED_ERROR)) }
    }
}
