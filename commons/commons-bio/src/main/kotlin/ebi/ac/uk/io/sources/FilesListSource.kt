package ebi.ac.uk.io.sources

import java.io.File

class FilesListSource(private val files: List<File>) : FilesSource {
    override fun getFile(path: String, md5: String?): NfsBioFile? =
        files.firstOrNull { it.name == path }?.let { NfsBioFile(it) }
}
