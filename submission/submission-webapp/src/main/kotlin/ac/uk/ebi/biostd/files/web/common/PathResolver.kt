package ac.uk.ebi.biostd.files.web.common

import ebi.ac.uk.base.removeFirstOccurrence
import ebi.ac.uk.model.GroupPath
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8

internal val groupPath = ".*/groups/[\\w'-]+/?".toRegex()

class GroupPathDescriptorResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == GroupPath::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ) = GroupPath(getPath(groupPath, getServletRequest(webRequest)))
}

private fun getServletRequest(webRequest: NativeWebRequest) = webRequest.getNativeRequest(HttpServletRequest::class.java)!!

private fun getPath(
    prefix: Regex,
    webRequest: HttpServletRequest,
): String =
    URLDecoder.decode(
        webRequest.requestURL
            .toString()
            .removeFirstOccurrence(prefix)
            .trim('/'),
        UTF_8.name(),
    )
