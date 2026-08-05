package ac.uk.ebi.biostd.tsv.deserialization.stream

import ac.uk.ebi.biostd.tsv.TAB
import ac.uk.ebi.biostd.validation.INVALID_FILES_TABLE
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.constants.TableFields.FILES_TABLE
import ebi.ac.uk.util.collections.destructure
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

internal class FileListTsvStreamDeserializer {
    suspend fun serializeFileList(
        files: Flow<BioFile>,
        fileList: OutputStream,
    ) {
        fileList.bufferedWriter().use { it.writeElements(files, FILES_TABLE.value, BioFile::path, BioFile::attributes) }
    }

    fun deserializeFileList(fileList: InputStream): Flow<BioFile> =
        fileList.readElements(FILES_TABLE.value, INVALID_FILES_TABLE) { index, row, headers ->
            deserializeFileListRow(index, row.split(TAB), headers)
        }

    private fun deserializeFileListRow(
        index: Int,
        row: List<String>,
        headers: List<String>,
    ): BioFile {
        val (path, attributes) = row.destructure()
        require(path.isNotBlank()) { throw InvalidElementException("Error at row ${index + 1}: $REQUIRED_FILE_PATH") }

        return BioFile(path, attributes = buildAttributes(attributes, headers))
    }
}
