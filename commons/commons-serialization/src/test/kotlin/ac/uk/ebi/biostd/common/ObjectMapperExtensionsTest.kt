package ac.uk.ebi.biostd.common

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.serialization.extensions.tryConvertValue

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class ObjectMapperExtensionsTest(
    @MockK val jsonNode: JsonNode,
    @MockK val javaType: JavaType,
    private val temporaryFolder: TemporaryFolder
) {

    @SpyK
    var testInstance: ObjectMapper = ObjectMapper()

    @Test
    fun tryToConvertWhenError() {
        every { testInstance.convertValue<Any>(jsonNode, javaType) } throws IllegalStateException()

        val result = testInstance.tryConvertValue(jsonNode, javaType)
        assertThat(result).isNull()
    }

    @Test
    fun tryToConvert() {
        every { testInstance.convertValue<Any>(jsonNode, javaType) } returns "a value"

        val result = testInstance.tryConvertValue(jsonNode, javaType)
        assertThat(result).isEqualTo("a value")
    }

//    @Test
//    fun `serialize - deserialize`() {
//        val foos = (1..3).map { Foo(it) }
//        val foosIterator = foos.iterator()
//        val file = temporaryFolder.createFile("test.txt")
//
//        file.outputStream().use { testInstance.serializeList(foos.asSequence(), it) }
//        file.inputStream().use {
//            testInstance.deserializeList<Sequence<Foo>>(it)
//                .forEach { foo -> assertThat(foo).isEqualTo(foosIterator.next()) }
//        }
//    }
}

data class Foo(val value: Int)
