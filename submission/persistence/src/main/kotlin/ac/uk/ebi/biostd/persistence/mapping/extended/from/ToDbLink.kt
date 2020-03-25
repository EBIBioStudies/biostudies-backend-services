package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.DbLink
import ac.uk.ebi.biostd.persistence.model.DbLinkAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtLink

internal fun ExtLink.toDbLink(order: Int, tableIndex: Int = NO_TABLE_INDEX) =
    DbLink(url, order, attributes.mapIndexedTo(sortedSetOf(), ::toLinkAttributeDb), tableIndex)

private fun toLinkAttributeDb(index: Int, attr: ExtAttribute) = DbLinkAttribute(attr.toDbAttribute(index))
