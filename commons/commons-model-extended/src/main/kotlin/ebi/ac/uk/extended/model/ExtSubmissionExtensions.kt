package ebi.ac.uk.extended.model

const val PROJECT_TYPE = "Project"

val ExtSubmission.allSections
    get(): List<ExtSection> = section.allSections + section

val ExtSubmission.allFileList
    get(): List<ExtFileList> = allSections.mapNotNull { it.fileList }

val ExtSubmission.allLinkList
    get(): List<ExtLinkList> = allSections.mapNotNull { it.linkList }

val ExtSubmission.allSectionsFiles
    get(): List<ExtFile> = allSections.flatMap { it.allInnerFiles }

val ExtSubmission.allPageTabFiles
    get(): List<ExtFile> = pageTabFiles + allFileList.flatMap { it.pageTabFiles } + allLinkList.flatMap { it.pageTabFiles }

val ExtSubmission.allInnerSubmissionFiles
    get(): List<ExtFile> = allSectionsFiles + allPageTabFiles

val ExtSubmission.isCollection
    get(): Boolean = section.type == PROJECT_TYPE

val ExtSubmission.computedTitle
    get(): String? = title ?: section.title

fun ExtSubmissionInfo.expectedFirePath(file: NfsFile): String = "$relPath/${file.relPath}"
