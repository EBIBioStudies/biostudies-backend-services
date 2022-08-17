package ebi.ac.uk.notifications.util

import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class TemplateLoaderTest(
    @MockK private val resource: Resource,
    @MockK private val resourceLoader: ResourceLoader,
    private val temporaryFolder: TemporaryFolder
) {
    private val testInstance = TemplateLoader(resourceLoader)

    @BeforeEach
    fun beforeEach() {
        val file = temporaryFolder.createFile("Default.txt", "submit")

        every { resource.inputStream } returns file.inputStream()
        every { resourceLoader.getResource("classpath:templates/Default.txt") } returns resource
    }

    @Test
    fun loadTemplate() {
        val template = testInstance.loadTemplate("Default.txt")

        assertThat(template).isEqualTo("submit")
        verify(exactly = 1) { resourceLoader.getResource("classpath:templates/Default.txt") }
    }
}
