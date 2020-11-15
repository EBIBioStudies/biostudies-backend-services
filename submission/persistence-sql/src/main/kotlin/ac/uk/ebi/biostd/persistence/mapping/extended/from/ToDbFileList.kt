package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.model.ReferencedFileList
import ebi.ac.uk.extended.model.ExtFileList

fun ExtFileList.toDbFileList(): ReferencedFileList =
    ReferencedFileList(fileName, files.mapIndexedTo(sortedSetOf()) { index, extFile -> extFile.toRefFile(index) })
