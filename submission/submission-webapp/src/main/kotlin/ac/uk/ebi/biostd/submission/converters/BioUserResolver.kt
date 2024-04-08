package ac.uk.ebi.biostd.submission.converters

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
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.getParameterAnnotation(BioUser::class.java) != null

    override fun resolveArgument(
        parameter: MethodParameter,
        container: ModelAndViewContainer?,
        request: NativeWebRequest,
        factory: WebDataBinderFactory?,
    ): SecurityUser = principalResolver.resolveArgument(parameter, container, request, factory) as SecurityUser
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@AuthenticationPrincipal
annotation class BioUser
