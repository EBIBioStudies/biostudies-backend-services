package ac.uk.ebi.transpiler.mapper

import ac.uk.ebi.transpiler.factory.filesTableTemplate
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

const val BASE_FOLDER = "RAW_DATA"

@ExtendWith(TemporaryFolderExtension::class)
class FilesTableTemplateMapperTest(private val temporaryFolder: TemporaryFolder) {
    private val testInstance = FilesTableTemplateMapper()

    @BeforeEach
    fun setUp() {
        temporaryFolder.createDirectory(BASE_FOLDER)
        setUpTestFiles(1)
        setUpTestFiles(2)
    }

    @Test
    fun map() {
        val filesTable =
            testInstance.map(filesTableTemplate(), "${temporaryFolder.root.absolutePath}/$BASE_FOLDER", BASE_FOLDER)

        assertThat(filesTable).isEqualTo(
            FilesTable(
                listOf(
                    createFile("$BASE_FOLDER/Plate1/rep1/A01/fig1.tiff", "Plate1", "rep1", "A01", "ynl003c", "pet8"),
                    createFile("$BASE_FOLDER/Plate2/rep2/A02/fig1.tiff", "Plate2", "rep2", "A02", "ybl104c", "sea4"),
                ),
            ),
        )
    }

    private fun createFile(
        path: String,
        vararg attributes: String,
    ) = BioFile(
        path,
        attributes =
            listOf(
                Attribute("Plate", attributes[0]),
                Attribute("Replicate", attributes[1]),
                Attribute("Well", attributes[2]),
                Attribute("Gene Identifier", attributes[3]),
                Attribute("Gene Symbol", attributes[4]),
            ),
    )

    private fun setUpTestFiles(idx: Int) {
        temporaryFolder.createDirectory("$BASE_FOLDER/Plate$idx")
        temporaryFolder.createDirectory("$BASE_FOLDER/Plate$idx/rep$idx")
        temporaryFolder.createDirectory("$BASE_FOLDER/Plate$idx/rep$idx/A0$idx")
        temporaryFolder.createFile("$BASE_FOLDER/Plate$idx/rep$idx/A0$idx/fig1.tiff")
    }
}
