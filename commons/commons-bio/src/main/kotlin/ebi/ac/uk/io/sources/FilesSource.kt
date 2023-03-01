package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import java.io.File

enum class PreferredSource { FIRE, SUBMISSION, USER_SPACE }

interface FilesSource {
    val description: String

    fun getExtFile(path: String, attributes: List<Attribute>): ExtFile?

    fun getFile(path: String): File?
}
