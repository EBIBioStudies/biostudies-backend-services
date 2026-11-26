package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtAttributeDetail

internal fun List<DocAttribute>.toExtAttributes(): List<ExtAttribute> = map { it.toExtAttribute() }

internal fun DocAttribute.toExtAttribute(): ExtAttribute =
    ExtAttribute(
        name,
        value,
        reference,
        nameAttrs.map { it.toExtDetail() },
        valueAttrs.map { it.toExtDetail() },
    )

internal fun DocAttributeDetail.toExtDetail(): ExtAttributeDetail = ExtAttributeDetail(name, value)
