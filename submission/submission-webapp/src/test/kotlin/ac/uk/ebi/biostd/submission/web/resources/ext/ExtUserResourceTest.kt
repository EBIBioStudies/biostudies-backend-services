package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.security.domain.service.ExtUserService
import ac.uk.ebi.biostd.security.web.ExtUserResource
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtUser
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class ExtUserResourceTest(
    @MockK private val extUserService: ExtUserService,
) {
    private val mvc =
        MockMvcBuilders
            .standaloneSetup(ExtUserResource(extUserService))
            .build()

    @Test
    fun `get ext user`() {
        val expectedJson =
            jsonObj {
                "login" to "test_user"
                "fullName" to "Test User"
                "email" to "test@ebi.ac.uk"
                "notificationsEnabled" to true
            }.toString()

        every { extUserService.getExtUser("test@ebi.ac.uk") } returns testUser()

        mvc.get("/security/users/extended/test@ebi.ac.uk") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(expectedJson) }
        }

        verify { extUserService.getExtUser("test@ebi.ac.uk") }
    }

    private fun testUser() =
        ExtUser(
            login = "test_user",
            fullName = "Test User",
            email = "test@ebi.ac.uk",
            notificationsEnabled = true,
        )
}
