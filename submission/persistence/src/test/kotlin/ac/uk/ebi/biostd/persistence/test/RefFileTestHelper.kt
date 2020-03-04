package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ebi.ac.uk.extended.model.ExtFile
import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Files

internal val extTestRefFile get() = ExtFile("fileName", "md5", createTempFile, listOf(extAttribute))

private val createTempFile
    get() = Files.createTempFile("file", ".tmp").toFile().apply { writeText("example text content") }

internal fun assertDbRefFile(file: DbReferencedFile, extFile: ExtFile, order: Int) {
    assertThat(file.name).isEqualTo(extFile.fileName)
    assertThat(file.order).isEqualTo(order)
    assertThat(file.size).isEqualTo(20)
    assertThat(file.attributes).hasSize(1)
    assertDbAttribute(file.attributes.first(), extAttribute)
}
