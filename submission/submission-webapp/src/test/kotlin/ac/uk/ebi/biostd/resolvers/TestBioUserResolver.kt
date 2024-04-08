package ac.uk.ebi.biostd.resolvers

import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

internal class TestBioUserResolver : HandlerMethodArgumentResolver {
    lateinit var securityUser: SecurityUser

    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.getParameterAnnotation(BioUser::class.java) != null

    override fun resolveArgument(
        parameter: MethodParameter,
        container: ModelAndViewContainer?,
        request: NativeWebRequest,
        factory: WebDataBinderFactory?,
    ): SecurityUser = securityUser
}
