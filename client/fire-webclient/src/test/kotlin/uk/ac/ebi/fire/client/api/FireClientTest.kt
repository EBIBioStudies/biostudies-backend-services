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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.fire.client.model.FireFile

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FireClientTest(
    private val tmpFolder: TemporaryFolder,
    @MockK private val template: RestTemplate
) {
    private val testInstance = FireClient(tmpFolder.root.absolutePath, template)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun save(@MockK fireFile: FireFile) {
        val file = tmpFolder.createFile("save-test.txt")
        val httpEntitySlot = slot<HttpEntity<LinkedMultiValueMap<String, Any>>>()

        every {
            template.postForObject(FIRE_OBJECTS_URL, capture(httpEntitySlot), FireFile::class.java)
        } returns fireFile

        testInstance.save(file, "the-md5")

        val httpEntity = httpEntitySlot.captured
        assertThat(httpEntity.headers[FIRE_MD5_HEADER]!!.first()).isEqualTo("the-md5")
        assertThat(httpEntity.headers[FIRE_SIZE_HEADER]!!.first()).isEqualTo(file.size().toString())
        assertThat(httpEntity.body!![FIRE_FILE_PARAM]!!.first()).isEqualTo(FileSystemResource(file))
        verify(exactly = 1) { template.postForObject(FIRE_OBJECTS_URL, capture(httpEntitySlot), FireFile::class.java) }
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
