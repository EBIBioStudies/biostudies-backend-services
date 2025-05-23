package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkList
import ac.uk.ebi.biostd.persistence.doc.model.LinkListDocLink
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkList
import org.bson.types.ObjectId
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.InputStream

class ToDocLinkListMapper(
    private val serializationService: ExtSerializationService,
) {
    internal fun convert(
        extLinkList: ExtLinkList,
        subId: ObjectId,
        accNo: String,
        version: Int,
    ): Pair<DocLinkList, List<LinkListDocLink>> {
        val links = extLinkList.file.inputStream().use { getLinks(it, extLinkList.filePath, subId, accNo, version) }
        val pageTabFiles = extLinkList.pageTabFiles.map { it.toDocFile() }

        return Pair(DocLinkList(extLinkList.filePath, pageTabFiles), links)
    }

    private fun getLinks(
        stream: InputStream,
        path: String,
        subId: ObjectId,
        accNo: String,
        version: Int,
    ): List<LinkListDocLink> =
        serializationService
            .deserializeLinkListAsSequence(stream)
            .mapIndexed { idx, link -> LinkListDocLink(ObjectId(), subId, asDocLink(link), path, idx, version, accNo) }
            .toList()

    private fun asDocLink(link: ExtLink) = DocLink(link.url, link.attributes.map { it.toDocAttribute() })
}
