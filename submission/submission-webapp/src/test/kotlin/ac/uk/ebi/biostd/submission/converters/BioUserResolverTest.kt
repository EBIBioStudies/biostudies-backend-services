package ac.uk.ebi.biostd.submission.converters

import ebi.ac.uk.api.REGISTER_PARAM
import ebi.ac.uk.api.USER_NAME_PARAM
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.exception.UnauthorizedOperation
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.MethodParameter
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer
import kotlin.reflect.jvm.javaMethod

@ExtendWith(MockKExtension::class)
internal class BioUserResolverTest(
    @MockK private val principalResolver: AuthenticationPrincipalArgumentResolver,
    @MockK private val securityService: ISecurityService
) {

    private val testInstance = BioUserResolver(principalResolver, securityService)

    @Nested
    @ExtendWith(MockKExtension::class)
    inner class SupportParameter {
        @Test
        fun `supports parameter when supported`() {
            val method = MethodParameter(DummyClass::method1.javaMethod, 0)
            assertThat(testInstance.supportsParameter(method)).isTrue()
        }

        @Test
        fun `supports parameter when not supported`() {
            val method = MethodParameter(DummyClass::method1.javaMethod, 1)
            assertThat(testInstance.supportsParameter(method)).isFalse()
        }
    }

    @Nested
    @ExtendWith(MockKExtension::class)
    inner class ResolveArgument {

        @MockK
        lateinit var parameter: MethodParameter

        @MockK
        lateinit var container: ModelAndViewContainer

        @MockK
        lateinit var request: NativeWebRequest

        @MockK
        lateinit var factory: WebDataBinderFactory

        @MockK
        lateinit var securityUser: SecurityUser

        @MockK
        lateinit var onBehalfUser: SecurityUser

        private val onBehalfUserEmail = "user@email.com"
        private val userName = "john Doe"

        @Test
        fun `bio user resolver`() {
            every { request.getParameter("sse") } returns null
        }

        @Test
        fun `resolve argument`() {
            every { securityUser.superuser } returns true
            every { principalResolver.resolveArgument(parameter, container, request, factory) } returns securityUser
            every { request.getParameter("onBehalf") } returns onBehalfUserEmail
            every { securityService.getUser(onBehalfUserEmail) } returns onBehalfUser

            val user = testInstance.resolveArgument(parameter, container, request, factory)
            assertThat(user).isEqualTo(onBehalfUser)
        }

        @Test
        fun `resolve argument when user is not admin`() {
            every { securityUser.superuser } returns false
            every { principalResolver.resolveArgument(parameter, container, request, factory) } returns securityUser
            every { request.getParameter("onBehalf") } returns onBehalfUserEmail

            assertThrows<UnauthorizedOperation> { testInstance.resolveArgument(parameter, container, request, factory) }
        }

        @Test
        fun `resolve argument when automated user registration`() {
            every { request.getParameter(REGISTER_PARAM) } returns "true"
            every { request.getParameter(USER_NAME_PARAM) } returns userName
            every { securityUser.superuser } returns true

            every { securityService.getOrCreateInactive(onBehalfUserEmail, userName) } returns onBehalfUser

            val user = testInstance.resolveArgument(parameter, container, request, factory)
            assertThat(user).isEqualTo(onBehalfUser)
        }
    }
}

class DummyClass {

    fun method1(@BioUser param1: SecurityUser, param2: SecurityUser) {}
}
