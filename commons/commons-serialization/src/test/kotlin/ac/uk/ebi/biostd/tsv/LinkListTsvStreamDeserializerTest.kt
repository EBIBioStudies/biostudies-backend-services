package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.stream.LinkListTsvStreamDeserializer
import ac.uk.ebi.biostd.validation.REQUIRED_LINK_URL
import ebi.ac.uk.dsl.tsv.Tsv
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createTempFile
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Link
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(TemporaryFolderExtension::class)
class LinkListTsvStreamDeserializerTest(
    private val tempFolder: TemporaryFolder,
) {
    private val testInstance = LinkListTsvStreamDeserializer()

    @Test
    fun `deserialize link list with empty spaces`() =
        runTest {
            val tsvFile =
                createTsvFile(
                    tsv {
                        line("Links", "Type")
                        line("https://example.org/test1", "link")
                        line()
                        line()
                        line("https://example.org/test2", "link2")
                        line()
                        line()
                    },
                )
            val links = tsvFile.inputStream().use { testInstance.deserializeLinkList(it).toList() }
            assertThat(links).hasSize(2)
            assertThat(links.first()).isEqualTo(Link("https://example.org/test1", listOf(Attribute("Type", "link"))))
            assertThat(links.second()).isEqualTo(Link("https://example.org/test2", listOf(Attribute("Type", "link2"))))
        }

    @Test
    fun deserialize() =
        runTest {
            val tsvFile =
                createTsvFile(
                    tsv {
                        line("Links", "Attr1", "Attr2")
                        line("https://example.org/link1", "A", "B")
                        line("https://example.org/link2", "C", "D")
                        line()
                    },
                )
            val links = tsvFile.inputStream().use { testInstance.deserializeLinkList(it).toList() }
            assertThat(links).containsExactly(
                Link("https://example.org/link1", listOf(Attribute("Attr1", "A"), Attribute("Attr2", "B"))),
                Link("https://example.org/link2", listOf(Attribute("Attr1", "C"), Attribute("Attr2", "D"))),
            )
        }

    @Test
    fun `deserialize with null values`() =
        runTest {
            val tsvFile =
                createTsvFile(
                    tsv {
                        line("Links", "Attr1", "Attr2")
                        line("https://example.org/link1", "A", "B")
                        line("https://example.org/link2", "C")
                        line()
                    },
                )
            val links = tsvFile.inputStream().use { testInstance.deserializeLinkList(it).toList() }
            assertThat(links).hasSize(2)
            assertThat(links.first()).isEqualTo(
                Link("https://example.org/link1", listOf(Attribute("Attr1", "A"), Attribute("Attr2", "B"))),
            )
            assertThat(links.second()).isEqualTo(Link("https://example.org/link2", listOf(Attribute("Attr1", "C"))))
        }

    @Test
    fun `deserialize with extra empty null values`() =
        runTest {
            val tsvFile =
                createTsvFile(
                    tsv {
                        line("Links", "Attr1", "Attr2")
                        line("https://example.org/link1", "A", "B")
                        line("https://example.org/link2", "C", "X", "")
                        line()
                    },
                )
            val links = tsvFile.inputStream().use { testInstance.deserializeLinkList(it).toList() }
            assertThat(links).hasSize(2)
            assertThat(links.first()).isEqualTo(
                Link("https://example.org/link1", listOf(Attribute("Attr1", "A"), Attribute("Attr2", "B"))),
            )
            assertThat(links.second()).isEqualTo(
                Link("https://example.org/link2", listOf(Attribute("Attr1", "C"), Attribute("Attr2", "X"))),
            )
        }

    private fun createTsvFile(content: Tsv): File {
        val file = tempFolder.root.createTempFile()
        file.writeText(content.toString())
        return file
    }

    @Test
    fun `serialize - deserialize LinkList`() =
        runTest {
            fun attributes(numberLink: Int) = (1..3).map { Attribute("attribute-$it", "attribute-$it-link$numberLink-value") }
            val links = (1..20_000).map { Link("https://example.org/link$it", attributes = attributes(it)) }
            val iterator = links.iterator()
            val output = tempFolder.createFile("testLink.tsv")
            output.outputStream().use { testInstance.serializeLinkList(links.asFlow(), it) }
            val result = output.inputStream().use { testInstance.deserializeLinkList(it).toList() }
            assertThat(result).allSatisfy { assertThat(it).usingRecursiveComparison().isEqualTo(iterator.next()) }
            assertThat(result).hasSize(20000)
        }

    @Test
    fun `serialize link list without attributes`() =
        runTest {
            val links = listOf(Link("https://example.org/link1"), Link("https://example.org/link2"))
            val output = tempFolder.createFile("no-attributes.tsv")
            output.outputStream().use { testInstance.serializeLinkList(links.asFlow(), it) }
            assertThat(output).hasContent(
                """
                Links
                https://example.org/link1
                https://example.org/link2
                """.trimIndent(),
            )
        }

    @Test
    fun `link list with empty url`() =
        runTest {
            val tsv =
                tsv {
                    line("Links", "Attr1", "Attr2")
                    line("https://example.org/link", "a", "b")
                    line("", "c", "d")
                    line()
                }
            val testFile = tempFolder.createFile("invalid.tsv", tsv.toString())
            val response = testFile.inputStream().use { runCatching { testInstance.deserializeLinkList(it).toList() } }
            assertThat(response.isFailure).isTrue()
            response.onFailure { assertThat(it.message).isEqualTo("Error at row 3: $REQUIRED_LINK_URL. Element was not created.") }
        }
}
