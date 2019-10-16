package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.File
import ebi.ac.uk.extended.model.ExtFile
import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Files

internal val extTestFile get() = ExtFile("fileName", createTempFile, listOf(extAttribute))

private val createTempFile
    get() = Files.createTempFile("file", ".tmp").toFile().apply { writeText("example text content") }

internal fun assertDbFile(file: File, extFile: ExtFile, order: Int, tableOrder: Int = NO_TABLE_INDEX) {
    assertThat(file.name).isEqualTo(extFile.fileName)
    assertThat(file.size).isEqualTo(20)
    assertThat(file.order).isEqualTo(order)
    assertThat(file.tableIndex).isEqualTo(tableOrder)

    assertThat(file.attributes).hasSize(1)
    assertDbAttribute(file.attributes.first(), extAttribute)
}
