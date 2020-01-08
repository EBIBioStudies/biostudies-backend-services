package ac.uk.ebi.biostd.submission.converters

import ebi.ac.uk.api.ON_BEHALF_PARAM
import ebi.ac.uk.api.REGISTER_PARAM
import ebi.ac.uk.api.USER_NAME_PARAM
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.exception.InvalidSseConfiguration
import ebi.ac.uk.security.integration.exception.UnauthorizedOperation
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.core.MethodParameter
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class BioUserResolver(
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
        val onBehalf = request.getParameter(ON_BEHALF_PARAM)
        val registerUser = request.getParameter(REGISTER_PARAM)?.toBoolean().orFalse()

        return when {
            registerUser -> getOrCreate(user, onBehalf, request.getParameter(USER_NAME_PARAM))
            onBehalf is String -> getUser(user, onBehalf)
            else -> user
        }
    }

    private fun getOrCreate(securityUser: SecurityUser, onBehalf: String?, name: String?): SecurityUser {
        if (securityUser.superuser.not()) throw UnauthorizedOperation("User need to be admin to use onBehalf option")
        if (onBehalf.isNullOrBlank()) throw InvalidSseConfiguration("'onBehalf'should contain email in register mode")
        if (name.isNullOrBlank()) throw InvalidSseConfiguration("'username' should contain user in register mode")
        return securityService.getOrCreateInactive(onBehalf, name)
    }

    private fun getUser(securityUser: SecurityUser, onBehalf: String): SecurityUser {
        if (securityUser.superuser.not()) throw UnauthorizedOperation("User need to be admin to use onBehalf option")
        return securityService.getUser(onBehalf)
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@AuthenticationPrincipal
annotation class BioUser
