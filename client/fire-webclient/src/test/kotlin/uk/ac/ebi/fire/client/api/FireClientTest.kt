package uk.ac.ebi.fire.client.api

import ebi.ac.uk.io.ext.size
import ebi.ac.uk.test.createFile
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
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import uk.ac.ebi.fire.client.exception.FireClientException
import uk.ac.ebi.fire.client.model.FireApiFile

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FireClientTest(
    private val tmpFolder: TemporaryFolder,
    @MockK private val template: RestTemplate
) {
    private val testInstance = FireClient(tmpFolder.root.absolutePath, template)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun save(@MockK fireFile: FireApiFile) {
        val file = tmpFolder.createFile("save-test.txt")
        val httpEntitySlot = slot<HttpEntity<LinkedMultiValueMap<String, Any>>>()

        every {
            template.postForObject(FIRE_OBJECTS_URL, capture(httpEntitySlot), FireApiFile::class.java)
        } returns fireFile

        testInstance.save(file, "the-md5")

        val httpEntity = httpEntitySlot.captured
        assertThat(httpEntity.headers[FIRE_MD5_HEADER]!!.first()).isEqualTo("the-md5")
        assertThat(httpEntity.headers[FIRE_SIZE_HEADER]!!.first()).isEqualTo(file.size().toString())
        assertThat(httpEntity.body!![FIRE_FILE_PARAM]!!.first()).isEqualTo(FileSystemResource(file))
        verify(exactly = 1) {
            template.postForObject(
                FIRE_OBJECTS_URL,
                capture(httpEntitySlot),
                FireApiFile::class.java
            )
        }
    }

    @Test
    fun `set path`() {
        val httpEntitySlot = slot<HttpEntity<String>>()

        every { template.put("$FIRE_OBJECTS_URL/the-fire-oid/firePath", capture(httpEntitySlot)) } answers { nothing }

        testInstance.setPath("the-fire-oid", "/a/new/path/file2.txt")

        val httpEntity = httpEntitySlot.captured
        assertThat(httpEntity.headers[FIRE_PATH_HEADER]!!.first()).isEqualTo("/a/new/path/file2.txt")
        verify(exactly = 1) { template.put("$FIRE_OBJECTS_URL/the-fire-oid/firePath", httpEntity) }
    }

    @Test
    fun `unset path`() {
        every { template.delete("$FIRE_OBJECTS_URL/the-fire-oid/firePath") } answers { nothing }

        testInstance.unsetPath("the-fire-oid")

        verify(exactly = 1) { template.delete("$FIRE_OBJECTS_URL/the-fire-oid/firePath") }
    }

    @Test
    fun `set bio metadata`() {
        val httpEntitySlot = slot<HttpEntity<String>>()

        every { template.put("$FIRE_OBJECTS_URL/fire-oid/metadata/set", capture(httpEntitySlot)) } answers { nothing }

        testInstance.setBioMetadata("fire-oid", "S-BSST0", "file", false)

        val httpEntity = httpEntitySlot.captured
        assertThat(httpEntity.headers[CONTENT_TYPE]!!.first()).isEqualTo(APPLICATION_JSON_VALUE)
        assertThat(httpEntity.body).isEqualTo(
            "{ \"$FIRE_BIO_ACC_NO\": \"S-BSST0\", \"$FIRE_BIO_FILE_TYPE\": \"file\", \"$FIRE_BIO_PUBLISHED\": false }"
        )
        verify(exactly = 1) { template.put("$FIRE_OBJECTS_URL/fire-oid/metadata/set", httpEntity) }
    }

    @Test
    fun `download by path`() {
        val file = tmpFolder.createFile("test.txt", "test content")

        every {
            template.getForObject("$FIRE_OBJECTS_URL/blob/path/S-BSST1/file1.txt", ByteArray::class.java)
        } returns file.readBytes()

        val downloadedFile = testInstance.downloadByPath("S-BSST1/file1.txt")

        assertThat(downloadedFile.readText()).isEqualTo("test content")
        assertThat(downloadedFile.absolutePath).isEqualTo("${tmpFolder.root.absolutePath}/file1.txt")
        verify(exactly = 1) {
            template.getForObject("$FIRE_OBJECTS_URL/blob/path/S-BSST1/file1.txt", ByteArray::class.java)
        }
    }

    @Test
    fun `download by fireId`() {
        val file = tmpFolder.createFile("test.txt", "test content")

        every {
            template.getForObject("$FIRE_OBJECTS_URL/blob/fireOId", ByteArray::class.java)
        } returns file.readBytes()

        val downloadedFile = testInstance.downloadByFireId("fireOId", "file1.txt")

        assertThat(downloadedFile.readText()).isEqualTo("test content")
        assertThat(downloadedFile.absolutePath).isEqualTo("${tmpFolder.root.absolutePath}/file1.txt")
        verify(exactly = 1) {
            template.getForObject("$FIRE_OBJECTS_URL/blob/fireOId", ByteArray::class.java)
        }
    }

    @Test
    fun `find by md5`(@MockK fireFile: FireApiFile) {
        every { template.getForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/md5/the-md5") } returns arrayOf(fireFile)

        val files = testInstance.findByMd5("the-md5")

        assertThat(files).hasSize(1)
        assertThat(files.first()).isEqualTo(fireFile)
        verify(exactly = 1) {
            template.getForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/md5/the-md5")
        }
    }

    @Test
    fun `find by accNo`(@MockK fireFile: FireApiFile) {
        val httpEntitySlot = slot<HttpEntity<String>>()
        every {
            template.postForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/metadata", capture(httpEntitySlot))
        } returns arrayOf(fireFile)

        val files = testInstance.findByAccNo("S-BSST0")

        val httpEntity = httpEntitySlot.captured
        assertThat(files).hasSize(1)
        assertThat(files.first()).isEqualTo(fireFile)
        assertThat(httpEntity.body).isEqualTo("{ \"$FIRE_BIO_ACC_NO\": \"S-BSST0\" }")
        assertThat(httpEntity.headers[CONTENT_TYPE]!!.first()).isEqualTo(APPLICATION_JSON_VALUE)
        verify(exactly = 1) {
            template.postForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/metadata", httpEntity)
        }
    }

    @Test
    fun `find by accNo and published`(@MockK fireFile: FireApiFile) {
        val httpEntitySlot = slot<HttpEntity<String>>()
        every {
            template.postForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/metadata", capture(httpEntitySlot))
        } returns arrayOf(fireFile)

        val files = testInstance.findByAccNoAndPublished("S-BSST0", true)

        val httpEntity = httpEntitySlot.captured
        assertThat(files).hasSize(1)
        assertThat(files.first()).isEqualTo(fireFile)
        assertThat(httpEntity.headers[CONTENT_TYPE]!!.first()).isEqualTo(APPLICATION_JSON_VALUE)
        assertThat(httpEntity.body).isEqualTo("{ \"$FIRE_BIO_ACC_NO\": \"S-BSST0\", \"$FIRE_BIO_PUBLISHED\": true }")
        verify(exactly = 1) {
            template.postForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/metadata", httpEntity)
        }
    }

    @Test
    fun `find by path`(@MockK fireFile: FireApiFile) {
        every { template.getForObject<FireApiFile>("$FIRE_OBJECTS_URL/path/my/path") } returns fireFile

        val file = testInstance.findByPath("my/path")

        assertThat(file).isEqualTo(fireFile)
        verify(exactly = 1) { template.getForObject<FireApiFile>("$FIRE_OBJECTS_URL/path/my/path") }
    }

    @Test
    fun `find all by path`(@MockK fireFile: FireApiFile) {
        every {
            template.getForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/entries/path/my/path")
        } returns arrayOf(fireFile)

        val files = testInstance.findAllInPath("my/path")

        assertThat(files).hasSize(1)
        assertThat(files.first()).isEqualTo(fireFile)
        verify(exactly = 1) {
            template.getForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/entries/path/my/path")
        }
    }

    @Test
    fun `find all by path when FireClientException with NOT_FOUND status code`() {
        every {
            template.getForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/entries/path/my/path")
        }.throws(FireClientException(NOT_FOUND, "no files found in the given path"))

        val files = testInstance.findAllInPath("my/path")

        assertThat(files).hasSize(0)
        verify(exactly = 1) {
            template.getForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/entries/path/my/path")
        }
    }

    @Test
    fun `find all by path when httpException without a status code other than NOT_FOUND`() {
        every {
            template.getForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/entries/path/my/path")
        }.throws(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        assertThatExceptionOfType(HttpClientErrorException::class.java)
            .isThrownBy { testInstance.findAllInPath("my/path") }

        verify(exactly = 1) {
            template.getForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/entries/path/my/path")
        }
    }

    @Test
    fun `find by path when FireClientException with NOT_FOUND status code`() {
        every {
            template.getForObject<FireApiFile>("$FIRE_OBJECTS_URL/path/my/path")
        }.throws(FireClientException(NOT_FOUND, "no file found with the given path"))

        val file = testInstance.findByPath("my/path")

        assertThat(file).isNull()
        verify(exactly = 1) { template.getForObject<FireApiFile>("$FIRE_OBJECTS_URL/path/my/path") }
    }

    @Test
    fun `find by path when httpException without a status code other than NOT_FOUND`() {
        every {
            template.getForObject<FireApiFile>("$FIRE_OBJECTS_URL/path/my/path")
        }.throws(HttpClientErrorException(HttpStatus.BAD_REQUEST))

        assertThrows<HttpClientErrorException> { testInstance.findByPath("my/path") }

        verify(exactly = 1) { template.getForObject<FireApiFile>("$FIRE_OBJECTS_URL/path/my/path") }
    }

    @Test
    fun publish() {
        every { template.put("$FIRE_OBJECTS_URL/the-fire-oid/publish", null) } answers { nothing }

        testInstance.publish("the-fire-oid")

        verify(exactly = 1) { template.put("$FIRE_OBJECTS_URL/the-fire-oid/publish", null) }
    }

    @Test
    fun unpublish() {
        every { template.delete("$FIRE_OBJECTS_URL/the-fire-oid/publish") } answers { nothing }

        testInstance.unpublish("the-fire-oid")

        verify(exactly = 1) { template.delete("$FIRE_OBJECTS_URL/the-fire-oid/publish") }
    }
}
