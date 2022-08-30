package ebi.ac.uk.extended.model

const val PROJECT_TYPE = "Project"

val ExtSubmission.allSections
    get(): List<ExtSection> = section.allSections + section

val ExtSubmission.allFileList
    get(): List<ExtFileList> = allSections.mapNotNull { it.fileList }

val ExtSubmission.allSectionsFiles
    get(): List<ExtFile> = allSections.flatMap { it.allFiles }

val ExtSubmission.allPageTabFiles
    get(): List<ExtFile> = pageTabFiles + allFileList.flatMap { it.pageTabFiles }

val ExtSubmission.isCollection
    get(): Boolean = section.type == PROJECT_TYPE

val ExtSubmission.computedTitle
    get(): String? = title ?: section.title
