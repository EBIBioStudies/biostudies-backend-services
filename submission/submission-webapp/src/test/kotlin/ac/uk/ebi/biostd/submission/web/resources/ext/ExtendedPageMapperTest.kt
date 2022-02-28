package ac.uk.ebi.biostd.submission.web.resources.ext

import ebi.ac.uk.extended.model.WebExtPage
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.net.URI

private const val BASE = "http://localhost:8080"

@ExtendWith(MockKExtension::class)
class ExtendedPageMapperTest {
    private val testInstance = ExtendedPageMapper(URI(BASE))

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `as basic ext page`(
        @MockK currentPageable: Pageable,
        @MockK extSubmission: ExtSubmission,
        @MockK currentPage: Page<ExtSubmission>,
        @MockK nextPageable: Pageable
    ) {
        val request = ExtPageRequest(offset = 1, limit = 1, released = true)

        every { currentPage.hasNext() } returns true
        every { currentPage.hasPrevious() } returns false
        mockNextPage(currentPage, nextPageable)
        mockCurrentPage(currentPage, currentPageable, extSubmission)

        val page = testInstance.asExtPage(currentPage, request)
        assertBasicPageAttributes(page, extSubmission)
        assertThat(page.next).isEqualTo("$BASE/submissions/extended?offset=2&limit=1&released=true")
        assertThat(page.previous).isNull()
    }

    @Test
    fun `as filtering ext page`(
        @MockK extSubmission: ExtSubmission,
        @MockK currentPage: Page<ExtSubmission>,
        @MockK currentPageable: Pageable,
        @MockK previousPageable: Pageable,
        @MockK nextPageable: Pageable
    ) {
        val from = "2019-09-21T15:03:45Z"
        val to = "2019-09-22T15:03:45Z"
        val request = ExtPageRequest(fromRTime = from, toRTime = to, offset = 1, limit = 1)

        mockCurrentPage(currentPage, currentPageable, extSubmission)
        mockPreviousPage(currentPage, previousPageable)
        mockNextPage(currentPage, nextPageable)

        val page = testInstance.asExtPage(currentPage, request)
        assertBasicPageAttributes(page, extSubmission)
        assertThat(page.next).isEqualTo("$BASE/submissions/extended?offset=2&limit=1&fromRTime=$from&toRTime=$to")
        assertThat(page.previous).isEqualTo("$BASE/submissions/extended?offset=0&limit=1&fromRTime=$from&toRTime=$to")
    }

    private fun assertBasicPageAttributes(page: WebExtPage, extSubmission: ExtSubmission) {
        assertThat(page.content).isEqualTo(listOf(extSubmission))
        assertThat(page.totalElements).isEqualTo(3)
        assertThat(page.offset).isEqualTo(1)
        assertThat(page.limit).isEqualTo(1)
    }

    private fun mockCurrentPage(
        currentPage: Page<ExtSubmission>,
        currentPageable: Pageable,
        extSubmission: ExtSubmission
    ) {
        every { currentPage.content } returns listOf(extSubmission)
        every { currentPage.totalElements } returns 3
        every { currentPage.pageable } returns currentPageable
        every { currentPageable.offset } returns 1
        every { currentPageable.pageSize } returns 1
    }

    private fun mockPreviousPage(currentPage: Page<ExtSubmission>, previousPageable: Pageable) {
        every { previousPageable.offset } returns 0
        every { previousPageable.pageSize } returns 1
        every { currentPage.hasPrevious() } returns true
        every { currentPage.previousPageable() } returns previousPageable
    }

    private fun mockNextPage(currentPage: Page<ExtSubmission>, nextPageable: Pageable) {
        every { nextPageable.offset } returns 2
        every { nextPageable.pageSize } returns 1
        every { currentPage.hasNext() } returns true
        every { currentPage.nextPageable() } returns nextPageable
    }
}
