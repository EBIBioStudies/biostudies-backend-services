package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.factory.TestSuperUser
import ac.uk.ebi.biostd.resolvers.TestBioUserResolver
import ac.uk.ebi.biostd.submission.converters.ExtPageSubmissionConverter
import ac.uk.ebi.biostd.submission.converters.ExtSubmissionConverter
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.web.model.ExtPage
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class)
class ExtSubmissionResourceTest(
    @MockK private val extSubmissionService: ExtSubmissionService,
    @MockK private val extPageMapper: ExtendedPageMapper,
    @MockK private val extSerializationService: ExtSerializationService
) {
    private val bioUserResolver = TestBioUserResolver()
    private val mvc = MockMvcBuilders
        .standaloneSetup(ExtSubmissionResource(extSubmissionService, extPageMapper))
        .setMessageConverters(
            ExtSubmissionConverter(extSerializationService),
            ExtPageSubmissionConverter(extSerializationService)
        )
        .setCustomArgumentResolvers(bioUserResolver)
        .build()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun getExtended(@MockK extSubmission: ExtSubmission) {
        val submissionJson = jsonObj { "accNo" to "S-TEST123" }.toString()
        bioUserResolver.securityUser = TestSuperUser.asSecurityUser()
        every { extSerializationService.serialize(extSubmission) } returns submissionJson
        every { extSubmissionService.getExtendedSubmission("S-TEST123") } returns extSubmission

        mvc.get("/submissions/extended/S-TEST123")
            .andExpect {
                status { isOk }
                content { json(submissionJson) }
            }

        verify { extSerializationService.serialize(extSubmission) }
        verify { extSubmissionService.getExtendedSubmission("S-TEST123") }
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

    @Test
    fun getExtendedSubmissions(
        @MockK extSubmission: ExtSubmission,
        @MockK pageable: Page<ExtSubmission>
    ) {
        val extPageRequestSlot = slot<ExtPageRequest>()
        val extPage = ExtPage(listOf(extSubmission), 1, 1, 0, null, null)
        val response = jsonObj {
            "content" to jsonArray(jsonObj { "accNo" to "S-TEST123" })
            "totalElements" to 1
            "limit" to 1
            "offset" to 0
        }.toString()

        every { extSerializationService.serialize(extPage) } returns response
        every { extPageMapper.asExtPage(pageable, capture(extPageRequestSlot)) } returns extPage
        every { extSubmissionService.getExtendedSubmissions(capture(extPageRequestSlot)) } returns pageable

        mvc.get("/submissions/extended?limit=1&offset=0&fromRTime=2019-09-21T15:03:45Z&toRTime=2019-09-22T15:03:45Z")
            .andExpect {
                status { isOk }
                content { json(response) }
            }

        val extPageRequest = extPageRequestSlot.captured
        verifyExtPageRequest(extPageRequest)
        verify(exactly = 1) { extPageMapper.asExtPage(pageable, extPageRequest) }
        verify(exactly = 1) { extSerializationService.serialize(extPage) }
        verify(exactly = 1) { extSubmissionService.getExtendedSubmissions(extPageRequest) }
    }

    private fun verifyExtPageRequest(query: ExtPageRequest) {
        assertThat(query.offset).isEqualTo(0)
        assertThat(query.limit).isEqualTo(1)
        assertThat(query.fromRTime).isEqualTo("2019-09-21T15:03:45Z")
        assertThat(query.toRTime).isEqualTo("2019-09-22T15:03:45Z")
    }
}
