package uk.ac.ebi.fire.client.api

import ebi.ac.uk.commons.http.ext.delete
import ebi.ac.uk.commons.http.ext.getForObject
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.BodyInserters.MultipartInserter
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import uk.ac.ebi.fire.client.model.FireApiFile
import java.util.function.Consumer

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FireWebClientTest(
    private val tmpFolder: TemporaryFolder,
    @MockK private val client: WebClient,
) {
    private val testInstance = FireWebClient(client)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun save(
        @MockK fireFile: FireApiFile,
        @MockK requestSpec: RequestBodySpec,
    ) {
        val bodySlot = slot<MultipartInserter>()
        val headersSlot = slot<Consumer<HttpHeaders>>()
        val file = tmpFolder.createFile("save-test.txt")

        every { client.post().uri("/objects") } returns requestSpec
        every { requestSpec.body(capture(bodySlot)) } answers { nothing }
        every { requestSpec.headers(capture(headersSlot)) } answers { nothing }
        every { requestSpec.retrieve().bodyToMono(FireApiFile::class.java).block() } returns fireFile

        testInstance.save(file, "the-md5", 55)

        val body = bodySlot.captured
        val headers = headersSlot.captured
        headers.andThen {
            assertThat(it[FIRE_MD5_HEADER]!!.first()).isEqualTo("the-md5")
            assertThat(it[FIRE_SIZE_HEADER]!!.first()).isEqualTo("55")
        }
        verify(exactly = 1) {
            client.post().uri("/objects")
            requestSpec.body(body)
            requestSpec.retrieve().bodyToMono(FireApiFile::class.java).block()
        }
    }

    @Test
    fun `set path`(
        @MockK requestSpec: RequestBodySpec,
    ) {
        val headersSlot = slot<Consumer<HttpHeaders>>()

        every { client.put().uri("/objects/the-fire-oid/firePath") } returns requestSpec
        every { requestSpec.headers(capture(headersSlot)) } answers { nothing }
        every { requestSpec.retrieve().bodyToMono(String::class.java).block() } answers { nothing }

        testInstance.setPath("the-fire-oid", "/a/new/path/file2.txt")

        val headers = headersSlot.captured
        headers.andThen {
            assertThat(it[FIRE_PATH_HEADER]!!.first()).isEqualTo("/a/new/path/file2.txt")
        }
        verify(exactly = 1) {
            client.put().uri("/objects/the-fire-oid/firePath")
            requestSpec.retrieve().bodyToMono(String::class.java).block()
        }
        verify(exactly = 0) {
            requestSpec.body(any())
        }
    }

    @Test
    fun `unset path`() {
        every { client.delete("/objects/the-fire-oid/firePath") } answers { nothing }

        testInstance.unsetPath("the-fire-oid")

        verify(exactly = 1) { client.delete("/objects/the-fire-oid/firePath") }
    }

    @Test
    fun `find by md5`(@MockK fireFile: FireApiFile) {
        every { client.getForObject<Array<FireApiFile>>("/objects/md5/the-md5") } returns arrayOf(fireFile)

        val files = testInstance.findByMd5("the-md5")

        assertThat(files).hasSize(1)
        assertThat(files.first()).isEqualTo(fireFile)
        verify(exactly = 1) {
            client.getForObject<Array<FireApiFile>>("/objects/md5/the-md5")
        }
    }

    @Test
    fun `find by path`(@MockK fireFile: FireApiFile) {
        every { client.getForObject<FireApiFile>("/objects/path/my/path") } returns fireFile

        val file = testInstance.findByPath("my/path")

        assertThat(file).isEqualTo(fireFile)
        verify(exactly = 1) { client.getForObject<FireApiFile>("/objects/path/my/path") }
    }

    @Test
    fun `find all by path`(@MockK fireFile: FireApiFile) {
        every {
            client.getForObject<Array<FireApiFile>>("/objects/entries/path/my/path")
        } returns arrayOf(fireFile)

        val files = testInstance.findAllInPath("my/path")

        assertThat(files).hasSize(1)
        assertThat(files.first()).isEqualTo(fireFile)
        verify(exactly = 1) {
            client.getForObject<Array<FireApiFile>>("/objects/entries/path/my/path")
        }
    }

    @Test
    fun `find all by path when FireClientException with NOT_FOUND status code`() {
        every {
            client.getForObject<Array<FireApiFile>>("/objects/entries/path/my/path")
        }.throws(HttpClientErrorException(NOT_FOUND, "no files found in the given path"))

        val files = testInstance.findAllInPath("my/path")

        assertThat(files).hasSize(0)
        verify(exactly = 1) {
            client.getForObject<Array<FireApiFile>>("/objects/entries/path/my/path")
        }
    }

    @Test
    fun `find all by path when httpException without a status code other than NOT_FOUND`() {
        every {
            client.getForObject<Array<FireApiFile>>("/objects/entries/path/my/path")
        }.throws(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        assertThatExceptionOfType(HttpClientErrorException::class.java)
            .isThrownBy { testInstance.findAllInPath("my/path") }

        verify(exactly = 1) {
            client.getForObject<Array<FireApiFile>>("/objects/entries/path/my/path")
        }
    }

    @Test
    fun `find by path when FireClientException with NOT_FOUND status code`() {
        every {
            client.getForObject<FireApiFile>("/objects/path/my/path")
        }.throws(HttpClientErrorException(NOT_FOUND, "no file found with the given path"))

        val file = testInstance.findByPath("my/path")

        assertThat(file).isNull()
        verify(exactly = 1) { client.getForObject<FireApiFile>("/objects/path/my/path") }
    }

    @Test
    fun `find by path when httpException without a status code other than NOT_FOUND`() {
        every {
            client.getForObject<FireApiFile>("/objects/path/my/path")
        }.throws(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        assertThrows<HttpClientErrorException> { testInstance.findByPath("my/path") }

        verify(exactly = 1) { client.getForObject<FireApiFile>("/objects/path/my/path") }
    }

    @Test
    fun publish(@MockK apiFile: FireApiFile) {
        every {
            client.putForObject<FireApiFile>("/objects/the-fire-oid/publish")
        } returns apiFile

        val response = testInstance.publish("the-fire-oid")

        assertThat(response).isEqualTo(apiFile)
    }

    @Test
    fun unpublish() {
        every { client.delete("/objects/the-fire-oid/publish") } answers { nothing }

        testInstance.unpublish("the-fire-oid")

        verify(exactly = 1) { client.delete("/objects/the-fire-oid/publish") }
    }

    @Test
    fun delete() {
        every { client.delete("/objects/the-fire-oid") } answers { nothing }

        testInstance.delete("the-fire-oid")

        verify(exactly = 1) { client.delete("/objects/the-fire-oid") }
    }
}
