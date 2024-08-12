package ac.uk.ebi.biostd.submission.converters

import ebi.ac.uk.api.ON_BEHALF_PARAM
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.REGISTER_PARAM
import ebi.ac.uk.api.USER_NAME_PARAM
import ebi.ac.uk.base.orFalse
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.net.URLDecoder
import kotlin.text.Charsets.UTF_8

class OnBehalfUserRequestResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = OnBehalfParameters::class.java == parameter.parameterType

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        request: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): OnBehalfParameters? {
        val onBehalf = request.getParameter(ON_BEHALF_PARAM)
        val onBehalfName = request.getParameter(USER_NAME_PARAM)?.let { URLDecoder.decode(it, UTF_8.name()) }
        val registerUser = request.getParameter(REGISTER_PARAM)?.toBoolean().orFalse()
        return if (onBehalf == null) null else OnBehalfParameters(onBehalf, onBehalfName, registerUser)
    }
}
