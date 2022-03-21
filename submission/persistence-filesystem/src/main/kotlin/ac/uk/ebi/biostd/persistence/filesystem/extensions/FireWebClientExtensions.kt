package ac.uk.ebi.biostd.persistence.filesystem.extensions

import ebi.ac.uk.io.ext.md5
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.FireFile
import java.io.File

internal fun FireWebClient.persistFireFile(accNo: String, file: File, path: String): FireFile {
    val fireFile = save(file, file.md5())

    setBioMetadata(fireFile.fireOid, accNo, published = false)
    setPath(fireFile.fireOid, path)

    return fireFile
}
