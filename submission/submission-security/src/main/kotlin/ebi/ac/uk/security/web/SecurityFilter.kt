package ebi.ac.uk.security.web

import arrow.core.Option
import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.base.toOption
import ebi.ac.uk.security.integration.components.ISecurityFilter
import ebi.ac.uk.security.integration.components.ISecurityQueryService
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

internal class SecurityFilter(
    private val environment: String,
    private val securityQueryService: ISecurityQueryService
) : GenericFilterBean(), ISecurityFilter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        getSecurityKey(request as HttpServletRequest)
            .map { securityQueryService.getUserProfile(it) }
            .map { (user, token) -> setSecurityUser(user, token) }
        chain.doFilter(request, response)
    }

    private fun setSecurityUser(user: SecurityUser, token: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, token, authorities(user))
    }

    private fun authorities(user: SecurityUser): List<GrantedAuthority> {
        return if (user.superuser) listOf(SimpleGrantedAuthority("ADMIN")) else emptyList()
    }

    private fun getSecurityKey(httpRequest: HttpServletRequest): Option<String> {
        val header: String? = httpRequest.getHeader(HEADER_NAME)
        val cookie = WebUtils.getCookie(httpRequest, "$COOKIE_NAME-$environment")

        return when {
            header.isNotBlank() -> header.toOption()
            cookie != null && cookie.value.isNotBlank() -> cookie.value.toOption()
            else -> httpRequest.getParameter(COOKIE_NAME).toOption()
        }
    }
}
