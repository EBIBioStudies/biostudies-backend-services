package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ac.uk.ebi.biostd.persistence.model.File
import ac.uk.ebi.biostd.persistence.model.ReferencedFile
import ebi.ac.uk.extended.model.ExtFile
import org.assertj.core.api.Assertions.assertThat
import java.io.File as SystemFile

private const val FILE_NAME = "fileName"

internal val fileDb get() = File("fileName", 1, 55L, sortedSetOf(fileAttributeDb))
internal val refRileDb get() = ReferencedFile("fileName", 0, 55L, sortedSetOf(refAttributeDb))

internal fun assertFileDb(extFile: ExtFile, systemFile: SystemFile) {
    assertThat(extFile.fileName).isEqualTo(FILE_NAME)
    assertThat(extFile.file).isEqualTo(systemFile)

    assertThat(extFile.attributes).hasSize(1)
    assertExtAttribute(extFile.attributes.first())
}
