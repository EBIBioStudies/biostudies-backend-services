package ebi.ac.uk.io.sources

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(MockKExtension::class)
internal class ComposedFileSourceTest(
    @MockK private val oneFileSource: FilesSource,
    @MockK private val anotherFileSource: FilesSource
) {
    private val testInstance = ComposedFileSource(oneFileSource, anotherFileSource)
    private val filePath = "a/file/path"

    @Nested
    inner class Exists {
        @Test
        fun existsWhenOne() {
            testWhenOne { assertThat(testInstance.exists(filePath)).isTrue() }
        }

        @Test
        fun existsWhenAnother() {
            testWhenAnother { assertThat(testInstance.exists(filePath)).isTrue() }
        }
    }

    @Nested
    inner class GetFile {
        private val file = File(filePath)

        @Test
        fun whenOne() {
            testWhenOne {
                every { oneFileSource.getFile(filePath) } returns file

                assertThat(testInstance.exists(filePath)).isTrue()
            }
        }

        @Test
        fun whenAnother() {
            testWhenOne {
                every { anotherFileSource.getFile(filePath) } returns file

                assertThat(testInstance.exists(filePath)).isTrue()
            }
        }
    }

    @Nested
    inner class Size {
        private val size = 55L

        @Test
        fun whenOne() {
            testWhenOne {
                every { oneFileSource.size(filePath) } returns size

                assertThat(testInstance.exists(filePath)).isTrue()
            }
        }

        @Test
        fun whenAnother() {
            testWhenOne {
                every { anotherFileSource.size(filePath) } returns size

                assertThat(testInstance.exists(filePath)).isTrue()
            }
        }
    }

    @Nested
    inner class ReadText {
        private val text = "file text"

        @Test
        fun whenOne() {
            testWhenOne {
                every { oneFileSource.readText(filePath) } returns text

                assertThat(testInstance.exists(filePath)).isTrue()
            }
        }

        @Test
        fun whenAnother() {
            testWhenOne {
                every { anotherFileSource.readText(filePath) } returns text

                assertThat(testInstance.exists(filePath)).isTrue()
            }
        }
    }

    private fun testWhenOne(test: () -> Unit) {
        every { oneFileSource.exists(filePath) } returns true
        every { anotherFileSource.exists(filePath) } returns false

        test()
    }

    private fun testWhenAnother(test: () -> Unit) {
        every { oneFileSource.exists(filePath) } returns false
        every { anotherFileSource.exists(filePath) } returns true

        test()
    }
}
