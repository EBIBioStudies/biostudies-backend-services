package ebi.ac.uk.security.web

import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.security.integration.components.SecurityFilter
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import org.springframework.web.util.WebUtils
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

const val HEADER_NAME = "X-Session-Token"
const val COOKIE_NAME = "BIOSTDSESS"

internal class SpringSecurityFilter(
    private val environment: String,
    private val securityQueryService: SecurityQueryService,
) : GenericFilterBean(),
    SecurityFilter {
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        getSecurityKey(request as HttpServletRequest)
            ?.let { securityQueryService.getUserProfile(it) }
            ?.let { (user, token) -> setSecurityUser(user, token) }
        chain.doFilter(request, response)
    }

    private fun setSecurityUser(
        user: SecurityUser,
        token: String,
    ) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, token, authorities(user))
    }

    private fun authorities(user: SecurityUser): List<GrantedAuthority> =
        if (user.superuser) listOf(SimpleGrantedAuthority("ADMIN")) else emptyList()

    private fun getSecurityKey(httpRequest: HttpServletRequest): String? {
        val header: String? = httpRequest.getHeader(HEADER_NAME)
        val cookie = WebUtils.getCookie(httpRequest, "$COOKIE_NAME-$environment")

        return when {
            header.isNotBlank() -> header
            cookie != null && cookie.value.isNotBlank() -> cookie.value
            else -> httpRequest.getParameter(COOKIE_NAME)
        }
    }
}
