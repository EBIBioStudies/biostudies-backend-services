package ac.uk.ebi.transpiler.mapper

import ac.uk.ebi.transpiler.factory.filesTableTemplate
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BioImagesMapperTest {
    private val testInstance = BioImagesMapper()

    @Test
    fun map() {
        val filesTable = testInstance.map(filesTableTemplate())

        assertThat(filesTable).isEqualTo(FilesTable(listOf(
            createFile("Plate1/rep1/A01", "Plate1", "rep1", "A01", "ynl003c", "pet8"),
            createFile("Plate2/rep2/A02", "Plate2", "rep2", "A02", "ybl104c", "sea4")
        )))
    }

    private fun createFile(path: String, vararg attributes: String) =
        File(path, attributes = listOf(
            Attribute("Plate", attributes[0]),
            Attribute("Replicate", attributes[1]),
            Attribute("Well", attributes[2]),
            Attribute("Gene Identifier", attributes[3]),
            Attribute("Gene Symbol", attributes[4])
        ))
}
