package ebi.ac.uk.security.web

import arrow.core.Option
import ebi.ac.uk.base.toOption
import ebi.ac.uk.security.integration.components.ISecurityFilter
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.security.service.SecurityService
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import org.springframework.web.util.WebUtils
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

const val HEADER_NAME = "X-Session-Token"
const val COOKIE_NAME = "BIOSTDSESS"

internal class SecurityFilter(
    private val environment: String,
    private val securityService: SecurityService
) : GenericFilterBean(), ISecurityFilter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        runCatching {
            getSecurityKey(request as HttpServletRequest)
                .map { securityService.getUserProfile(it) }
                .map { (user, token) -> setSecurityUser(user, token) }
            chain.doFilter(request, response)
        }.onFailure {
            (response as HttpServletResponse).status = HttpStatus.UNAUTHORIZED.value()
            response.writer.write(it.message ?: it.localizedMessage)
        }
    }

    private fun setSecurityUser(user: SecurityUser, token: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, token, emptyList())
    }

    private fun getSecurityKey(httpRequest: HttpServletRequest): Option<String> {
        val header: String? = httpRequest.getHeader(HEADER_NAME)
        val cookie = WebUtils.getCookie(httpRequest, "$COOKIE_NAME-$environment")

        return when {
            header.isNullOrBlank().not() -> header.toOption()
            cookie != null && cookie.value.isNotBlank() -> cookie.value.toOption()
            else -> httpRequest.getParameter(COOKIE_NAME).toOption()
        }
    }
}
