package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocLinkList
import ac.uk.ebi.biostd.persistence.doc.model.LinkListDocLink
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkList
import org.bson.types.ObjectId

class ToDocLinkListMapper {
    internal fun convert(extLinkList: ExtLinkList): DocLinkList {
        val pageTabFiles = extLinkList.pageTabFiles.map { it.toDocFile() }

        return DocLinkList(extLinkList.filePath, pageTabFiles)
    }

    internal fun toDocLink(
        link: ExtLink,
        linkListName: String,
        index: Int,
        submissionId: ObjectId,
        submissionAccNo: String,
        submissionVersion: Int,
    ): LinkListDocLink =
        LinkListDocLink(
            id = ObjectId(),
            submissionId = submissionId,
            link = link.toDocLink(),
            linkListName = linkListName,
            index = index,
            submissionAccNo = submissionAccNo,
            submissionVersion = submissionVersion,
        )
}
