package ac.uk.ebi.biostd.persistence.filesystem.extensions

import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FileType
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File

internal fun FireClient.persistFireFile(
    accNo: String,
    file: File,
    type: FileType,
    md5: String,
    path: String,
): FireApiFile {
    val fireFile = save(file, md5)
    setBioMetadata(fireFile.fireOid, accNo, type, published = false)
    setPath(fireFile.fireOid, path)

    return fireFile
}
