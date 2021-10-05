package ebi.ac.uk.extended.model

const val PROJECT_TYPE = "Project"

val ExtSubmission.allSections
    get(): List<ExtSection> = section.allSections + section

val ExtSubmission.allFileList
    get(): List<ExtFileList> = allSections.mapNotNull { it.fileList }

val ExtSubmission.allFiles
    get(): List<ExtFile> = allSections.flatMap { it.allFiles }

val ExtSubmission.isCollection
    get(): Boolean = section.type == PROJECT_TYPE

val ExtSubmission.filesPath
    get(): String = "$relPath/Files"
