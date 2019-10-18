package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.model.Link
import ac.uk.ebi.biostd.persistence.model.LinkAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtLink

internal fun ExtLink.toDbLink(order: Int, tableIndex: Int = NO_TABLE_INDEX) =
    Link(url, order, attributes.mapIndexedTo(sortedSetOf(), ::toLinkAttributeDb), tableIndex)

private fun toLinkAttributeDb(index: Int, attr: ExtAttribute) = LinkAttribute(attr.toDbAttribute(index))
