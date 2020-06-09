package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.factory.TestSuperUser
import ac.uk.ebi.biostd.resolvers.TestBioUserResolver
import ac.uk.ebi.biostd.submission.converters.ExtSubmissionConverter
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class)
class ExtSubmissionResourceTest(
    @MockK private val extSubmissionService: ExtSubmissionService,
    @MockK private val extSerializationService: ExtSerializationService
) {
    private val bioUserResolver = TestBioUserResolver()
    private val mvc = MockMvcBuilders
        .standaloneSetup(ExtSubmissionResource(extSubmissionService))
        .setMessageConverters(ExtSubmissionConverter(extSerializationService))
        .setCustomArgumentResolvers(bioUserResolver)
        .build()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun getExtended(@MockK extSubmission: ExtSubmission) {
        val submissionJson = jsonObj { "accNo" to "S-TEST123" }.toString()
        bioUserResolver.securityUser = TestSuperUser.asSecurityUser()
        every { extSerializationService.serialize(extSubmission) } returns submissionJson
        every { extSubmissionService.getExtendedSubmission("admin_user@ebi.ac.uk", "S-TEST123") } returns extSubmission

        mvc.get("/submissions/extended/S-TEST123")
            .andExpect {
                status { isOk }
                content { json(submissionJson) }
            }

        verify { extSerializationService.serialize(extSubmission) }
        verify { extSubmissionService.getExtendedSubmission("admin_user@ebi.ac.uk", "S-TEST123") }
    }

    @Test
    fun submitExtended(@MockK extSubmission: ExtSubmission) {
        val user = TestSuperUser.asSecurityUser()
        val submissionJson = jsonObj { "accNo" to "S-TEST123" }.toString()

        bioUserResolver.securityUser = user
        every { extSerializationService.serialize(extSubmission) } returns submissionJson
        every { extSubmissionService.submitExtendedSubmission(user.email, extSubmission) } returns extSubmission
        every { extSerializationService.deserialize(submissionJson, ExtSubmission::class.java) } returns extSubmission

        mvc.post("/submissions/extended") {
            content = submissionJson
        }.andExpect {
            status { isOk }
            content { json(submissionJson) }
        }

        verify { extSerializationService.serialize(extSubmission) }
        verify { extSerializationService.deserialize(submissionJson, ExtSubmission::class.java) }
        verify { extSubmissionService.submitExtendedSubmission(user.email, extSubmission) }
    }
}
