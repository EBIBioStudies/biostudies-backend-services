package ac.uk.ebi.biostd.files.web

import ebi.ac.uk.base.remove
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import javax.servlet.http.HttpServletRequest

private const val PATH_SEPARATOR = "/"
internal val userPath = ".*${PATH_SEPARATOR}user".toRegex()
internal val groupPath = ".*${PATH_SEPARATOR}groups".toRegex()

class PathDescriptorResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == PathDescriptor::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory
    ): Any {
        val path = getServletRequest(webRequest)
            .requestURL.toString()
            .remove(userPath)
            .removePrefix(PATH_SEPARATOR)

        return PathDescriptor(path)
    }

    private fun getServletRequest(webRequest: NativeWebRequest) =
        webRequest.getNativeRequest(HttpServletRequest::class.java)!!
}

class PathDescriptor(val path: String)
