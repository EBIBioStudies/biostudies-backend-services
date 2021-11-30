package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.NfsFile
import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Files

internal val extTestRefFile
    get() = NfsFile(
        "folder/fileName",
        "Files/folder/fileName",
        "root/Files/folder/fileName",
        createTempFile,
        listOf(extAttribute)
    )

private val createTempFile
    get() = Files.createTempFile("file", ".tmp").toFile().apply { writeText("example text content") }

internal fun assertDbRefFile(file: DbReferencedFile, extFile: ExtFile, order: Int) {
    assertThat(file.name).isEqualTo((extFile as NfsFile).filePath)
    assertThat(file.order).isEqualTo(order)
    assertThat(file.size).isEqualTo(20)
    assertThat(file.attributes).hasSize(1)
    assertDbAttribute(file.attributes.first(), extAttribute)
}
