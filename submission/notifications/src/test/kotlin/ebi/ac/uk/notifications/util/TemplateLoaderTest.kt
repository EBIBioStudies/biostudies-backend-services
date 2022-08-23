package ebi.ac.uk.notifications.util

import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class TemplateLoaderTest(
    temporaryFolder: TemporaryFolder,
    @MockK private val resourceLoader: ResourceLoader,
) {
    private val testInstance = TemplateLoader(resourceLoader)
    private val testTemplate = temporaryFolder.createFile("TestTemplate.txt", "test-template")
    private val defaultTemplate = temporaryFolder.createFile("Default.txt", "default-template")
    private val collectionTemplate = temporaryFolder.createFile("ArrayExpress.txt", "array-express-template")

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun loadTemplate(
        @MockK resource: Resource,
    ) {
        every { resource.inputStream } returns testTemplate.inputStream()
        every { resourceLoader.getResource("classpath:templates/TestTemplate.txt") } returns resource

        val template = testInstance.loadTemplate("TestTemplate.txt")

        assertThat(template).isEqualTo("test-template")
        verify(exactly = 1) { resourceLoader.getResource("classpath:templates/TestTemplate.txt") }
    }

    @Test
    fun `load collection template`(
        @MockK collection: ExtCollection,
        @MockK submission: ExtSubmission,
        @MockK collectionResource: Resource,
    ) {
        every { collection.accNo } returns "ArrayExpress"
        every { collectionResource.exists() } returns true
        every { submission.collections } returns listOf(collection)
        every { collectionResource.inputStream } returns collectionTemplate.inputStream()
        every { resourceLoader.getResource("classpath:templates/ArrayExpress.txt") } returns collectionResource

        val template = testInstance.loadTemplateOrDefault(submission, "%s.txt")

        assertThat(template).isEqualTo("array-express-template")

        verify(exactly = 0) { resourceLoader.getResource("classpath:templates/Default.txt") }
        verify(exactly = 1) { resourceLoader.getResource("classpath:templates/ArrayExpress.txt") }
    }

    @Test
    fun `load default template`(
        @MockK collection: ExtCollection,
        @MockK submission: ExtSubmission,
        @MockK defaultResource: Resource,
        @MockK collectionResource: Resource,
    ) {
        every { collection.accNo } returns "ArrayExpress"
        every { collectionResource.exists() } returns false
        every { submission.collections } returns listOf(collection)
        every { defaultResource.inputStream } returns defaultTemplate.inputStream()
        every { resourceLoader.getResource("classpath:templates/Default.txt") } returns defaultResource
        every { resourceLoader.getResource("classpath:templates/ArrayExpress.txt") } returns collectionResource

        val template = testInstance.loadTemplateOrDefault(submission, "%s.txt")

        assertThat(template).isEqualTo("default-template")

        verify(exactly = 1) {
            resourceLoader.getResource("classpath:templates/Default.txt")
            resourceLoader.getResource("classpath:templates/ArrayExpress.txt")
        }
    }
}
