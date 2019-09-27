package ebi.ac.uk.io.sources

class ComposedFileSource(private vararg val sources: FilesSource) : FilesSource {
    override fun exists(filePath: String) = sources.any { it.exists(filePath) }

    override fun getFile(filePath: String) = sources.first { it.exists(filePath) }.getFile(filePath)

    override fun size(filePath: String): Long = sources.first { it.exists(filePath) }.size(filePath)

    override fun readText(filePath: String): String = sources.first { it.exists(filePath) }.readText(filePath)
}
