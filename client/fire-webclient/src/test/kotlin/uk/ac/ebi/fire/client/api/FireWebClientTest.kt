package uk.ac.ebi.fire.client.api

import ebi.ac.uk.commons.http.ext.getForObject
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.ac.ebi.fire.client.model.FireApiFile
import java.util.function.Consumer

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FireWebClientTest(
    private val tmpFolder: TemporaryFolder,
    @MockK private val client: WebClient,
    @MockK private val fireFile: FireApiFile,
    @MockK private val response: ClientResponse,
    @MockK private val requestSpec: RequestBodySpec,
) {
    private val testInstance = FireWebClient(client)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun save() = runTest {
        val bodySlot = slot<LinkedMultiValueMap<String, Any>>()
        val headersSlot = slot<Consumer<HttpHeaders>>()
        val file = tmpFolder.createFile("save-test.txt")

        every { client.post().uri("/objects") } returns requestSpec
        every { requestSpec.bodyValue(capture(bodySlot)) } returns requestSpec
        every { requestSpec.headers(capture(headersSlot)) } returns requestSpec
        every { requestSpec.retrieve().bodyToMono<FireApiFile>() } returns Mono.just(fireFile)

        testInstance.save(file, "the-md5", 55)

        val body = bodySlot.captured
        val headers = headersSlot.captured
        assertThat(body[FIRE_FILE_PARAM]!!.first()).isEqualTo(FileSystemResource(file))
        headers.andThen {
            assertThat(it[FIRE_MD5_HEADER]!!.first()).isEqualTo("the-md5")
            assertThat(it[FIRE_SIZE_HEADER]!!.first()).isEqualTo("55")
        }
        verify(exactly = 1) {
            client.post().uri("/objects")
            requestSpec.bodyValue(body)
            requestSpec.retrieve().bodyToMono<FireApiFile>()
        }
    }

    @Test
    fun `set path`() = runTest {
        val headersSlot = slot<Consumer<HttpHeaders>>()

        every { client.put().uri("/objects/the-fire-oid/firePath") } returns requestSpec
        every { requestSpec.headers(capture(headersSlot)) } returns requestSpec
        every { requestSpec.retrieve().bodyToMono<FireApiFile>() } returns Mono.just(fireFile)

        val response = testInstance.setPath("the-fire-oid", "/a/new/path/file2.txt")

        val headers = headersSlot.captured
        headers.andThen {
            assertThat(it[FIRE_PATH_HEADER]!!.first()).isEqualTo("/a/new/path/file2.txt")
        }
        verify(exactly = 1) {
            client.put().uri("/objects/the-fire-oid/firePath")
            requestSpec.retrieve().bodyToMono<FireApiFile>()
        }
        verify(exactly = 0) {
            requestSpec.body(any())
        }
        assertThat(response).isEqualTo(fireFile)
    }

    @Test
    fun `unset path`() = runTest {
        every { response.statusCode() } returns OK
        every { client.delete().uri("/objects/the-fire-oid/firePath").exchange() } returns Mono.just(response)

        testInstance.unsetPath("the-fire-oid")

        verify(exactly = 1) {
            client.delete().uri("/objects/the-fire-oid/firePath").exchange()
        }
    }

    @Test
    fun `find by md5 sync`() {
        every { client.getForObject<Array<FireApiFile>>("/objects/md5/the-md5") } returns arrayOf(fireFile)

        val files = testInstance.findByMd5Sync("the-md5")

        assertThat(files).hasSize(1)
        assertThat(files.first()).isEqualTo(fireFile)
        verify(exactly = 1) {
            client.getForObject<Array<FireApiFile>>("/objects/md5/the-md5")
        }
    }

    @Test
    fun `find by md5`() = runTest {
        every {
            client.get().uri("/objects/md5/the-md5").retrieve().bodyToMono<Array<FireApiFile>>()
        } returns Mono.just(arrayOf(fireFile))

        val files = testInstance.findByMd5("the-md5")

        assertThat(files).hasSize(1)
        assertThat(files.first()).isEqualTo(fireFile)
        verify(exactly = 1) {
            client.get().uri("/objects/md5/the-md5").retrieve().bodyToMono<Array<FireApiFile>>()
        }
    }

    @Test
    fun `find by path`() = runTest {
        every {
            client.get().uri("/objects/path/my/path").retrieve().bodyToMono<FireApiFile>()
        } returns Mono.just(fireFile)

        val file = testInstance.findByPath("my/path")

        assertThat(file).isEqualTo(fireFile)
        verify(exactly = 1) {
            client.get().uri("/objects/path/my/path").retrieve().bodyToMono<FireApiFile>()
        }
    }

    @Test
    fun `find all by path`() = runTest {
        every {
            client.get().uri("/objects/entries/path/my/path").retrieve().bodyToMono<Array<FireApiFile>>()
        } returns Mono.just(arrayOf(fireFile))

        val files = testInstance.findAllInPath("my/path")

        assertThat(files).hasSize(1)
        assertThat(files.first()).isEqualTo(fireFile)
        verify(exactly = 1) {
            client.get().uri("/objects/entries/path/my/path").retrieve().bodyToMono<Array<FireApiFile>>()
        }
    }

    @Test
    fun `find all by path when FireClientException with NOT_FOUND status code`() = runTest {
        every {
            client.get().uri("/objects/entries/path/my/path").retrieve().bodyToMono<Array<FireApiFile>>()
        } throws WebClientResponseException(NOT_FOUND.value(), "no files found in the given path", null, null, null)

        val files = testInstance.findAllInPath("my/path")

        assertThat(files).hasSize(0)
        verify(exactly = 1) {
            client.get().uri("/objects/entries/path/my/path").retrieve().bodyToMono<Array<FireApiFile>>()
        }
    }

    @Test
    fun `find all by path when httpException without a status code other than NOT_FOUND`() = runTest {
        every {
            client.get().uri("/objects/entries/path/my/path").retrieve().bodyToMono<Array<FireApiFile>>()
        } throws WebClientResponseException(BAD_REQUEST.value(), "no files found in the given path", null, null, null)

        assertThrows<WebClientResponseException> { testInstance.findAllInPath("my/path") }

        verify(exactly = 1) {
            client.get().uri("/objects/entries/path/my/path").retrieve().bodyToMono<Array<FireApiFile>>()
        }
    }

    @Test
    fun `find by path when FireClientException with NOT_FOUND status code`() = runTest {
        every {
            client.get().uri("/objects/path/my/path").retrieve().bodyToMono<FireApiFile>()
        } throws WebClientResponseException(NOT_FOUND.value(), "no files found in the given path", null, null, null)

        val file = testInstance.findByPath("my/path")

        assertThat(file).isNull()
        verify(exactly = 1) {
            client.get().uri("/objects/path/my/path").retrieve().bodyToMono<FireApiFile>()
        }
    }

    @Test
    fun `find by path when httpException without a status code other than NOT_FOUND`() = runTest {
        every {
            client.get().uri("/objects/path/my/path").retrieve().bodyToMono<FireApiFile>()
        } throws WebClientResponseException(BAD_REQUEST.value(), "no files found in the given path", null, null, null)

        assertThrows<WebClientResponseException> { testInstance.findByPath("my/path") }

        verify(exactly = 1) {
            client.get().uri("/objects/path/my/path").retrieve().bodyToMono<FireApiFile>()
        }
    }

    @Test
    fun publish() = runTest {
        every {
            client.put().uri("/objects/the-fire-oid/publish").retrieve().bodyToMono<FireApiFile>()
        } returns Mono.just(fireFile)

        val response = testInstance.publish("the-fire-oid")

        assertThat(response).isEqualTo(fireFile)
    }

    @Test
    fun unpublish() = runTest {
        every { response.statusCode() } returns OK
        every { client.delete().uri("/objects/the-fire-oid/publish").exchange() } returns Mono.just(response)

        testInstance.unpublish("the-fire-oid")

        verify(exactly = 1) {
            client.delete().uri("/objects/the-fire-oid/publish").exchange()
        }
    }

    @Test
    fun delete() = runTest {
        every { response.statusCode() } returns OK
        every { client.delete().uri("/objects/the-fire-oid").exchange() } returns Mono.just(response)

        testInstance.delete("the-fire-oid")

        verify(exactly = 1) {
            client.delete().uri("/objects/the-fire-oid").exchange()
        }
    }
}
