package ebi.ac.uk.security.web

import arrow.core.Option
import ebi.ac.uk.base.toOption
import ebi.ac.uk.model.User
import ebi.ac.uk.paths.FolderResolver
import ebi.ac.uk.security.integration.components.ISecurityFilter
import ebi.ac.uk.security.service.SecurityService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import org.springframework.web.util.WebUtils
import java.nio.file.Path
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import ac.uk.ebi.biostd.persistence.model.User as UserDb

const val HEADER_NAME = "X-Session-Token"
const val COOKIE_NAME = "BIOSTDSESS"

internal class SecurityFilter(
    private val environment: String,
    private val securityService: SecurityService,
    private val folderResolver: FolderResolver
) : GenericFilterBean(), ISecurityFilter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        getSecurityKey(request as HttpServletRequest)
            .flatMap { securityService.checkToken(it) }
            .map { setSecurityUser(User(it.id, it.email, it.secret, getMagicFolder(it)), it.secret) }
        chain.doFilter(request, response)
    }

    private fun getMagicFolder(user: UserDb): Path {
        return folderResolver.getUserMagicFolderPath(user.id, user.secret)
    }

    private fun setSecurityUser(user: User, key: String) {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, key, emptyList())
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
