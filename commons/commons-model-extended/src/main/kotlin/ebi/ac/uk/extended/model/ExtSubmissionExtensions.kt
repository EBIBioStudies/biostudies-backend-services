package ebi.ac.uk.extended.model

const val PROJECT_TYPE = "Project"

val ExtSubmission.allSections
    get(): List<ExtSection> = section.allSections + section

val ExtSubmission.allReferencedFiles
    get(): List<ExtFile> = allSections.flatMap { it.allReferencedFiles }

val ExtSubmission.allFiles
    get(): List<ExtFile> = allSections.flatMap { it.allFiles }

val ExtSubmission.allFileList
    get(): List<ExtFileList> = allSections.mapNotNull { it.fileList }

val ExtSubmission.isProject
    get(): Boolean = section.type == PROJECT_TYPE
