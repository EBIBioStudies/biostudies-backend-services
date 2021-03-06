package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.DbFile
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.NfsFile
import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Files

internal val extTestFile get() = NfsFile("fileName", createTempFile, listOf(extAttribute))

private val createTempFile
    get() = Files.createTempFile("file", ".tmp").toFile().apply { writeText("example text content") }

internal fun assertDbFile(file: DbFile, extFile: ExtFile, order: Int, tableOrder: Int = NO_TABLE_INDEX) {
    assertThat(file.name).isEqualTo((extFile as NfsFile).fileName)
    assertThat(file.size).isEqualTo(20)
    assertThat(file.order).isEqualTo(order)
    assertThat(file.tableIndex).isEqualTo(tableOrder)

    assertThat(file.attributes).hasSize(1)
    assertDbAttribute(file.attributes.first(), extAttribute)
}
