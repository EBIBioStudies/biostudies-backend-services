package ac.uk.ebi.biostd.security.web.exception

import ac.uk.ebi.biostd.security.web.dto.SecurityError
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private const val AUTHORIZATION_ERROR = "Problems authorizing user."

class SecurityAuthEntryPoint(private val mapper: ObjectMapper) : AuthenticationEntryPoint {
    override fun commence(request: HttpServletRequest, response: HttpServletResponse, excep: AuthenticationException) {
        response.write(UNAUTHORIZED) { mapper.writeValue(it, SecurityError(excep.message ?: AUTHORIZATION_ERROR)) }
    }
}
