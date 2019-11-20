package ac.uk.ebi.biostd.submission.converters

import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.core.MethodParameter
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class SubmitterResolver(
    private val principalResolver: AuthenticationPrincipalArgumentResolver,
    private val securityService: ISecurityService

) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.getParameterAnnotation(BioUser::class.java) != null
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        container: ModelAndViewContainer?,
        request: NativeWebRequest,
        factory: WebDataBinderFactory?
    ): Any? {
        val user = principalResolver.resolveArgument(parameter, container, request, factory) as SecurityUser
        return when (val onBehalf = request.getParameter("onBehalf")) {
            is String -> getUser(user, onBehalf)
            else -> user
        }
    }

    private fun getUser(securityUser: SecurityUser, onBehalf: String): SecurityUser {
        require(securityUser.superuser) { "User need to be admin to use onBehalf option" }
        return securityService.getUser(onBehalf)
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@AuthenticationPrincipal
annotation class BioUser
