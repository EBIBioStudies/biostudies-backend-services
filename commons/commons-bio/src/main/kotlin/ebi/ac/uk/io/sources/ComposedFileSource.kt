package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FileNotFoundException

class ComposedFileSource(private val sources: List<FilesSource>) : FilesSource {
    override fun exists(filePath: String) = sources.any { it.exists(filePath) }

    override fun getFile(filePath: String) = findFile(filePath).getFile(filePath)

    override fun readText(filePath: String): String = findFile(filePath).readText(filePath)

    private fun findFile(filePath: String) =
        sources.firstOrNull { it.exists(filePath) } ?: throw FileNotFoundException(filePath)
}
