package ac.uk.ebi.biostd.files.web.common

import ac.uk.ebi.biostd.files.web.fileListPath
import ac.uk.ebi.biostd.files.web.groupPath
import ac.uk.ebi.biostd.files.web.userPath
import ebi.ac.uk.base.removeFirstOccurrence
import ebi.ac.uk.model.FileListPath
import ebi.ac.uk.model.GroupPath
import ebi.ac.uk.model.UserPath
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8

class UserPathDescriptorResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == UserPath::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ) = UserPath(getPath(userPath, getServletRequest(webRequest)))
}

class GroupPathDescriptorResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == GroupPath::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ) = GroupPath(getPath(groupPath, getServletRequest(webRequest)))
}

class FileListPathDescriptorResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == FileListPath::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ) = FileListPath(getPath(fileListPath, getServletRequest(webRequest)))
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
