package ac.uk.ebi.biostd.files.web

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer
import javax.servlet.http.HttpServletRequest

private const val API_PATH = "http://localhost:8080/biostd/files"

@ExtendWith(MockKExtension::class)
class PathDescriptorResolverTest(
    @MockK val mockParameter: MethodParameter,
    @MockK val mockMavContainer: ModelAndViewContainer,
    @MockK val mockBinderFactory: WebDataBinderFactory,
    @MockK val mockWebRequest: NativeWebRequest,
    @MockK val mockHttpServletRequest: HttpServletRequest
) {
    private val testInstance = PathDescriptorResolver()

    @BeforeEach
    fun init() {
        every { mockWebRequest.getNativeRequest(HttpServletRequest::class.java) } returns mockHttpServletRequest
    }

    @Test
    fun `path descriptor for user`() {
        assertPathDescriptor("$API_PATH/user", "")
    }

    @Test
    fun `path descriptor for user ending in slash`() {
        assertPathDescriptor("$API_PATH/user/", "")
    }

    @Test
    fun `path descriptor for user folder`() {
        assertPathDescriptor("$API_PATH/user/folder1", "folder1")
    }

    @Test
    fun `path descriptor for user inner folder`() {
        assertPathDescriptor("$API_PATH/user/folder1/folder2", "folder1/folder2")
    }

    private fun assertPathDescriptor(request: String, expectedPath: String) {
        every { mockHttpServletRequest.requestURL } returns StringBuffer(request)

        val descriptor = testInstance.resolveArgument(mockParameter, mockMavContainer, mockWebRequest, mockBinderFactory)

        assertThat(descriptor).isInstanceOf(PathDescriptor::class.java)
        assertThat((descriptor as PathDescriptor).path).isEqualTo(expectedPath)
    }
}
