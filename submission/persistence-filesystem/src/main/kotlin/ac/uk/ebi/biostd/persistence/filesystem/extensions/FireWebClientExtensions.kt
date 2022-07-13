package ac.uk.ebi.biostd.persistence.filesystem.extensions

import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File

internal fun FireClient.persistFireFile(file: File, md5: String, size: Long, path: String): FireApiFile {
    val fireFile = save(file, md5, size)
    setPath(fireFile.fireOid, path)
    return fireFile
}
