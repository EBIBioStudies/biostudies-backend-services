package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.factory.TestSuperUser
import ac.uk.ebi.biostd.files.web.common.FileListPathDescriptorResolver
import ac.uk.ebi.biostd.resolvers.TestBioUserResolver
import ac.uk.ebi.biostd.submission.converters.ExtFileTableConverter
import ac.uk.ebi.biostd.submission.converters.ExtPageSubmissionConverter
import ac.uk.ebi.biostd.submission.converters.ExtSubmissionConverter
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.helpers.TempFileGenerator
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.WebExtPage
import ebi.ac.uk.model.constants.SUBMISSION
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.multipart.MultipartFile
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class)
class ExtSubmissionResourceTest(
    @MockK private val tempFileGenerator: TempFileGenerator,
    @MockK private val extSubmissionService: ExtSubmissionService,
    @MockK private val extPageMapper: ExtendedPageMapper,
    @MockK private val extSerializationService: ExtSerializationService,
    @MockK private val extSubmissionQueryService: ExtSubmissionQueryService,
) {
    private val bioUserResolver = TestBioUserResolver()
    private val mvc = MockMvcBuilders
        .standaloneSetup(
            ExtSubmissionResource(
                extPageMapper,
                extSubmissionService,
                extSubmissionQueryService,
                extSerializationService
            )
        )
        .setMessageConverters(
            ExtFileTableConverter(extSerializationService),
            ExtSubmissionConverter(extSerializationService),
            ExtPageSubmissionConverter(extSerializationService)
        )
        .setCustomArgumentResolvers(bioUserResolver, FileListPathDescriptorResolver())
        .build()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun getExtended(@MockK extSubmission: ExtSubmission) {
        val submissionJson = jsonObj { "accNo" to "S-TEST123" }.toString()
        bioUserResolver.securityUser = TestSuperUser.asSecurityUser()
        every { extSerializationService.serialize(extSubmission) } returns submissionJson
        coEvery { extSubmissionQueryService.getExtendedSubmission("S-TEST123") } returns extSubmission

        mvc.get("/submissions/extended/S-TEST123")
            .asyncDispatch()
            .andExpect {
                status { isOk() }
                content { json(submissionJson) }
            }

        coVerify(exactly = 1) {
            extSerializationService.serialize(extSubmission)
            extSubmissionQueryService.getExtendedSubmission("S-TEST123")
        }
    }

    @Test
    fun submitExtended(@MockK extSubmission: ExtSubmission) {
        val user = TestSuperUser.asSecurityUser()
        val fileLists = slot<List<MultipartFile>>()
        val submissionJson = jsonObj { "accNo" to "S-TEST123" }.toString()

        bioUserResolver.securityUser = user
        every { tempFileGenerator.asFiles(capture(fileLists)) } returns emptyList()
        every { extSerializationService.serialize(extSubmission) } returns submissionJson
        coEvery { extSubmissionService.submitExt(user.email, extSubmission) } returns extSubmission
        every { extSerializationService.deserialize(submissionJson) } returns extSubmission

        mvc.multipart("/submissions/extended") {
            content = submissionJson
            param(SUBMISSION, submissionJson)
        }.asyncDispatch().andExpect {
            status { isOk() }
            content { json(submissionJson) }
        }

        coVerify(exactly = 1) {
            extSerializationService.serialize(extSubmission)
            extSubmissionService.submitExt(user.email, extSubmission)
            extSerializationService.deserialize(submissionJson)
        }
    }

    @Test
    fun submitExtendedAsync(@MockK extSubmission: ExtSubmission) {
        val user = TestSuperUser.asSecurityUser()
        val fileLists = slot<List<MultipartFile>>()
        val submissionJson = jsonObj { "accNo" to "S-TEST123" }.toString()

        bioUserResolver.securityUser = user
        every { tempFileGenerator.asFiles(capture(fileLists)) } returns emptyList()
        coEvery { extSubmissionService.submitExtAsync(user.email, extSubmission) } answers { nothing }
        every { extSerializationService.deserialize(submissionJson) } returns extSubmission

        mvc.multipart("/submissions/extended/async") {
            content = submissionJson
            param(SUBMISSION, submissionJson)
        }.asyncDispatch().andExpect {
            status { isOk() }
        }

        coVerify(exactly = 1) {
            extSubmissionService.submitExtAsync(user.email, extSubmission)
            extSerializationService.deserialize(submissionJson)
        }
    }

    @Test
    fun `get referenced files from file list`(
        @MockK extFileTable: ExtFileTable,
    ) {
        val filesJson = jsonObj { "files" to "ext-file-table" }.toString()

        every { extSerializationService.serialize(extFileTable) } returns filesJson
        coEvery { extSubmissionQueryService.getReferencedFiles("S-TEST123", "file-list") } returns extFileTable

        mvc.get("/submissions/extended/S-TEST123/referencedFiles/file-list")
            .asyncDispatch()
            .andExpect {
                status { isOk() }
                content { json(filesJson) }
            }

        coVerify(exactly = 1) {
            extSerializationService.serialize(extFileTable)
            extSubmissionQueryService.getReferencedFiles("S-TEST123", "file-list")
        }
    }

    @Test
    fun `get referenced files from file list inside inner folder`(
        @MockK extFileTable: ExtFileTable,
    ) {
        val filesJson = jsonObj { "files" to "ext-file-table" }.toString()

        every { extSerializationService.serialize(extFileTable) } returns filesJson
        coEvery {
            extSubmissionQueryService.getReferencedFiles(
                "S-TEST123",
                "my/folder/file-list"
            )
        } returns extFileTable

        mvc.get("/submissions/extended/S-TEST123/referencedFiles/my/folder/file-list")
            .asyncDispatch()
            .andExpect {
                status { isOk() }
                content { json(filesJson) }
            }

        coVerify(exactly = 1) {
            extSerializationService.serialize(extFileTable)
            extSubmissionQueryService.getReferencedFiles("S-TEST123", "my/folder/file-list")
        }
    }

    @Test
    fun getExtendedSubmissions(
        @MockK extSubmission: ExtSubmission,
        @MockK pageable: Page<ExtSubmission>,
    ) {
        val extPageRequestSlot = slot<ExtPageRequest>()
        val webExtPage = WebExtPage(listOf(extSubmission), 1, 1, 0, null, null)
        val response = jsonObj {
            "content" to jsonArray(jsonObj { "accNo" to "S-TEST123" })
            "totalElements" to 1
            "limit" to 1
            "offset" to 0
        }.toString()

        every { extSerializationService.serialize(webExtPage) } returns response
        every { extPageMapper.asExtPage(pageable, capture(extPageRequestSlot)) } returns webExtPage
        coEvery { extSubmissionQueryService.getExtendedSubmissions(capture(extPageRequestSlot)) } returns pageable

        mvc.get("/submissions/extended?limit=1&offset=0&fromRTime=2019-09-21T15:03:45Z&toRTime=2019-09-22T15:03:45Z")
            .asyncDispatch()
            .andExpect {
                status { isOk() }
                content { json(response) }
            }

        val extPageRequest = extPageRequestSlot.captured
        verifyExtPageRequest(extPageRequest)
        coVerify(exactly = 1) {
            extSerializationService.serialize(webExtPage)
            extPageMapper.asExtPage(pageable, extPageRequest)
            extSubmissionQueryService.getExtendedSubmissions(extPageRequest)
        }
    }

    private fun verifyExtPageRequest(query: ExtPageRequest) {
        assertThat(query.offset).isEqualTo(0)
        assertThat(query.limit).isEqualTo(1)
        assertThat(query.fromRTime).isEqualTo("2019-09-21T15:03:45Z")
        assertThat(query.toRTime).isEqualTo("2019-09-22T15:03:45Z")
    }
}
