package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileOrigin
import ebi.ac.uk.extended.model.ExtFileOrigin.MIXED
import ebi.ac.uk.model.Attribute
import java.io.File

class ComposedFileSource(
    private val sources: List<FilesSource>,
    private val preferredOrigin: ExtFileOrigin
) : FilesSource {
    override val filesOrigin: ExtFileOrigin
        get() = MIXED

    // TODO remove preferred origin from the method
    // TODO test fire
    override fun getExtFile(
        path: String,
        md5: String?,
        attributes: List<Attribute>,
        preferredOrigin: ExtFileOrigin
    ): ExtFile? {
        val sorted = sources.filter { it.isPreferred() } to sources.filterNot { it.isPreferred() }

        return findFile(path, md5, attributes, sorted.first) ?: findFile(path, md5, attributes, sorted.second)
    }

    private fun FilesSource.isPreferred() = filesOrigin == preferredOrigin

    private fun findFile(
        path: String,
        md5: String?,
        attributes: List<Attribute>,
        sources: List<FilesSource>
    ) = sources.firstNotNullOfOrNull { it.getExtFile(path, md5, attributes) }

    override fun getFile(path: String, md5: String?): File? =
        sources.firstNotNullOfOrNull { it.getFile(path, md5) }
}
