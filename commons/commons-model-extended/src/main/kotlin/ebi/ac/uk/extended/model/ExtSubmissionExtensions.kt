package ebi.ac.uk.extended.model

import ebi.ac.uk.util.date.toStringDate

val ExtSubmission.allSections
    get(): List<ExtSection> = section.allSections + section

val ExtSubmission.allReferencedFiles
    get(): List<ExtFile> = allSections.flatMap { it.allReferencedFiles }

val ExtSubmission.allFiles
    get(): List<ExtFile> = allSections.flatMap { it.allFiles }

val ExtSubmission.allFileListSections
    get(): List<ExtFileList> = allSections.mapNotNull { it.fileList }

val ExtSubmission.releaseDate
    get(): String = releaseTime?.toStringDate() ?: ""
