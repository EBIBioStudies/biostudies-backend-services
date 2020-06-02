package ac.uk.ebi.biostd.files.web

import ebi.ac.uk.base.remove
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8
import javax.servlet.http.HttpServletRequest

internal val userPath = ".*/user".toRegex()
internal val groupPath = ".*/groups/[\\w'-]+/?".toRegex()

class PathDescriptorResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == PathDescriptor::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory
    ): Any {
        val path = getPath(getServletRequest(webRequest))
            .remove(userPath)
            .removePrefix("/")

        return PathDescriptor(path)
    }

    private fun getServletRequest(webRequest: NativeWebRequest) =
        webRequest.getNativeRequest(HttpServletRequest::class.java)!!

    private fun getPath(webRequest: HttpServletRequest) =
        URLDecoder.decode(webRequest.requestURL.toString(), UTF_8.name())
}

class PathDescriptor(val path: String)
