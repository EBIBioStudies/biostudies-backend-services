package ebi.ac.uk.security.web

import arrow.core.Option
import ebi.ac.uk.base.toOption
import ebi.ac.uk.model.User
import ebi.ac.uk.security.util.TokenUtil
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
    private val tokenUtil: TokenUtil
) : GenericFilterBean() {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        getSecurityKey(request as HttpServletRequest)
                .flatMap { tokenUtil.fromToken(it) }
                .map { setSecurityUser(User(it.id, it.email, it.secret), it.secret) }
        chain.doFilter(request, response)
    }

    private fun setSecurityUser(user: User, key: String) {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, key, emptyList())
    }

    private fun getSecurityKey(httpRequest: HttpServletRequest): Option<String> {
        val header: String? = httpRequest.getHeader(HEADER_NAME)
        if (!header.isNullOrBlank()) {
            return header.toOption()
        }

        val cookie = WebUtils.getCookie(httpRequest, "$COOKIE_NAME-$environment")
        if (cookie != null && cookie.value.isNotBlank()) {
            return cookie.value.toOption()
        }

        return httpRequest.getParameter(COOKIE_NAME).toOption()
    }
}
