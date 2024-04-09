package uk.ac.ebi.fire.client.api

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectInputStream
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.apache.http.client.methods.HttpRequestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class S3ClientTest(
    private val tmpFolder: TemporaryFolder,
    @MockK val amazonS3Client: AmazonS3,
) {
    private val testInstance = S3Client("bucket", amazonS3Client)

    @Test
    fun downloadByPath(
        @MockK s3Object: S3Object,
        @MockK httpRequestBase: HttpRequestBase,
    ) {
        val content = tmpFolder.createFile("s3file", "content")
        val getRequestSlot = slot<GetObjectRequest>()
        every { amazonS3Client.getObject(capture(getRequestSlot)) } returns s3Object
        every { s3Object.objectContent } returns S3ObjectInputStream(content.inputStream(), httpRequestBase)

        val file = testInstance.downloadByPath("expectedPath")

        assertThat(file).hasContent("content")
    }
}
