package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocLinkList
import ebi.ac.uk.extended.model.ExtLinkList
import uk.ac.ebi.serialization.common.FilesResolver

class ToExtLinkListMapper(
    private val extFilesResolver: FilesResolver,
) {
    @Suppress("LongParameterList")
    fun toExtLinkList(
        linkList: DocLinkList,
        accNo: String,
        version: Int,
        released: Boolean,
        subRelPath: String,
    ): ExtLinkList =
        ExtLinkList(
            filePath = linkList.fileName,
            file = extFilesResolver.createRequestTempFile(accNo, version, linkList.fileName),
            pageTabFiles = linkList.pageTabFiles.map { it.toExtFile(released, subRelPath) },
        )
}
