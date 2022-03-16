package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FileNotFoundException

class ComposedFileSource(
    private val sources: List<FilesSource>,
    override val rootPath: String?
) : FilesSource {
    override fun exists(filePath: String): Boolean = sources.any { it.exists(filePath) }

    override fun getFile(filePath: String): BioFile = findFile(filePath).getFile(filePath)

    private fun findFile(filePath: String) =
        sources.firstOrNull { it.exists(filePath) } ?: throw FileNotFoundException(fullPath(filePath))
}
