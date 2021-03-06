package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FileNotFoundException
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(MockKExtension::class)
internal class ComposedFileSourceTest(
    @MockK private val oneFileSource: FilesSource,
    @MockK private val anotherFileSource: FilesSource
) {
    private val testInstance = ComposedFileSource(listOf(oneFileSource, anotherFileSource))
    private val filePath = "path/to/a/file.txt"

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

        @Test
        fun existsWhenNone() {
            testWhenNone { assertThat(testInstance.exists(filePath)).isFalse() }
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

        @Test
        fun whenNone() {
            testWhenNone {
                val exception = assertThrows<FileNotFoundException> { testInstance.getFile(filePath) }
                assertThat(exception.message).isEqualTo("File not found: $filePath")
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

        @Test
        fun whenNone() {
            testWhenNone {
                val exception = assertThrows<FileNotFoundException> { testInstance.readText(filePath) }
                assertThat(exception.message).isEqualTo("File not found: $filePath")
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

    private fun testWhenNone(test: () -> Unit) {
        every { oneFileSource.exists(filePath) } returns false
        every { anotherFileSource.exists(filePath) } returns false

        test()
    }
}
