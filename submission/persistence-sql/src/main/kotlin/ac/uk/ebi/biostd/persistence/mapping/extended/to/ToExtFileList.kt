package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.ReferencedFileList
import ebi.ac.uk.extended.model.ExtFileList

fun ReferencedFileList.toExtFileList(): ExtFileList = ExtFileList(name, emptyList())
