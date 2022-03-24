package ebi.ac.uk.io.sources

class ComposedFileSource(private val sources: List<FilesSource>) : FilesSource {
    override fun getFile(path: String, md5: String?): BioFile? = sources.firstNotNullOfOrNull { it.getFile(path) }
}
