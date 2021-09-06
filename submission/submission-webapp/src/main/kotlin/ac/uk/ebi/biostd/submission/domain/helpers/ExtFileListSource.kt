package ac.uk.ebi.biostd.submission.domain.helpers

import ebi.ac.uk.errors.FileNotFoundException
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.sources.BioFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.FireBioFile
import ebi.ac.uk.io.sources.NfsBioFile
import uk.ac.ebi.fire.client.integration.web.FireWebClient

class ExtFileListSource(
    private val fireWebClient: FireWebClient,
    private val files: List<ExtFile>
) : FilesSource {
    override fun exists(filePath: String): Boolean = files.any { it.fileName == filePath }

    override fun getFile(filePath: String): BioFile {
        val file = files.firstOrNull { it.fileName == filePath } ?: throw FileNotFoundException(filePath)
        return when (file) {
            is FireFile -> FireBioFile(
                file.fireId,
                file.md5,
                file.size,
                lazy { fireWebClient.downloadByFireId(file.fireId, file.fileName).readText() }
            )
            is NfsFile -> NfsBioFile(file.file)
        }
    }
}
