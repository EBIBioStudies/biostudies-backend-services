package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FileNotFoundException
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile

class BioListFilesSource(private val files: List<ExtFile>) : FilesSource {
    override fun exists(filePath: String): Boolean {
        return files.any { it.fileName == filePath }
    }

    override fun getFile(filePath: String): BioFile {
        val file = files.firstOrNull { it.fileName == filePath } ?: throw FileNotFoundException(filePath)
        return when (file) {
            is FireFile -> FireBioFile(file.fireId, file.md5)
            is NfsFile -> NfsBioFile(file.file)
        }
    }
}