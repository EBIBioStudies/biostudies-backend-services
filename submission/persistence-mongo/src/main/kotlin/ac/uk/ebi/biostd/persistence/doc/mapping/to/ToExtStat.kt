package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import ebi.ac.uk.extended.model.ExtStat

internal fun DocStat.toExtStat(): ExtStat = ExtStat(name, value.toString())
