package ac.uk.ebi.biostd.persistence.doc.test

import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTableRow
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.basicDocAttribute
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.fullDocAttribute
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileList
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.nfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.LinkTestHelper.docLink
import ebi.ac.uk.base.Either.Companion.left
import ebi.ac.uk.base.Either.Companion.right
import org.bson.types.ObjectId

internal const val SECT_ACC_NO = "SECT-001"
internal const val SECT_TYPE = "Study"
internal const val SUB_SECT_ACC_NO = "Exp1"
internal const val SUB_SECT_TYPE = "Study"

internal object SectionTestHelper {
    private val docSubSection =
        DocSection(
            id = ObjectId(),
            accNo = SUB_SECT_ACC_NO,
            type = SUB_SECT_TYPE,
            attributes = listOf(fullDocAttribute),
        )

    private val docTableSection =
        DocSectionTableRow(
            accNo = SUB_SECT_ACC_NO,
            type = SUB_SECT_TYPE,
            attributes = listOf(fullDocAttribute),
        )

    val docSection =
        DocSection(
            id = ObjectId(),
            accNo = SECT_ACC_NO,
            type = SECT_TYPE,
            fileList = docFileList,
            attributes = listOf(basicDocAttribute),
            sections = listOf(left(docSubSection), right(DocSectionTable(listOf(docTableSection)))),
            files = listOf(left(nfsDocFile)),
            links = listOf(left(docLink)),
        )
}
