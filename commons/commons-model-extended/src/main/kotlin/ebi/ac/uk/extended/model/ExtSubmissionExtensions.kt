package ebi.ac.uk.extended.model

val ExtSubmission.allSections
    get(): List<ExtSection> = section.allSections + section

val ExtSubmission.allReferencedFiles
    get(): List<ExtFile> = allSections.flatMap { it.allReferencedFiles }

val ExtSubmission.allFiles
    get(): List<ExtFile> = allSections.flatMap { it.allFiles }

val ExtSubmission.allFileLists
    get(): List<ExtFileList> = allSections.mapNotNull { it.fileList }
