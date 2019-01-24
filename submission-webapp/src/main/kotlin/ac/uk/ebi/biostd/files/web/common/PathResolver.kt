package ac.uk.ebi.biostd.files.web.common

import ebi.ac.uk.base.remove
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import javax.servlet.http.HttpServletRequest

private val USER_PATH = ".*/user".toRegex()

class UserPathDescriptorResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == UserPath::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory
    ) = UserPath(getPath(getServletRequest(webRequest)))
}

class GroupPathDescriptorResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == GroupPath::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory
    ) = GroupPath(getPath(getServletRequest(webRequest)))
}

private fun getServletRequest(webRequest: NativeWebRequest) =
    webRequest.getNativeRequest(HttpServletRequest::class.java)!!

private fun getPath(webRequest: HttpServletRequest) =
    webRequest
        .requestURL.toString()
        .remove(USER_PATH)
        .removePrefix("/")

class UserPath(val path: String)
class GroupPath(val path: String)
