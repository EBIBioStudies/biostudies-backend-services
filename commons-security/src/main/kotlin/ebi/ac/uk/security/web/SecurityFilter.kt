package ebi.ac.uk.security.web

import ebi.ac.uk.arrow.ifPresent
import ebi.ac.uk.model.User
import ebi.ac.uk.security.service.SecurityService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import org.springframework.web.util.WebUtils
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

const val HEADER_NAME = "X-Session-Token"
const val COOKIE_NAME = "BIOSTDSESS"

class SecurityFilter(
    private val environment: String,
    private val securityService: SecurityService
) : GenericFilterBean() {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val key = getSecurityKey(request as HttpServletRequest)
        securityService.fromToken(key).ifPresent { setSecurityUser(User(it.id, it.email, it.secret), key) }
        chain.doFilter(request, response)
    }

    private fun setSecurityUser(user: User, key: String) {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, key, emptyList())
    }

    private fun getSecurityKey(httpRequest: HttpServletRequest): String {
        val header = httpRequest.getHeader(HEADER_NAME)
        if (header.isNotBlank()) {
            return header
        }

        val cookie = WebUtils.getCookie(httpRequest, "$COOKIE_NAME-$environment")
        if (cookie != null && cookie.value.isNotBlank()) {
            return cookie.value
        }

        return httpRequest.getParameter(COOKIE_NAME).orEmpty()
    }
}