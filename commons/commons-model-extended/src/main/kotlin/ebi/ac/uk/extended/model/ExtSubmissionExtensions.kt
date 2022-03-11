package ebi.ac.uk.extended.model

const val PROJECT_TYPE = "Project"

val ExtSubmission.allSections
    get(): List<ExtSection> = section.allSections + section

val ExtSubmission.allFileList
    get(): List<ExtFileList> = allSections.mapNotNull { it.fileList }

val ExtSubmission.allSectionsFiles
    get(): List<ExtFile> = allSections.flatMap { it.allFiles }

val ExtSubmission.isCollection
    get(): Boolean = section.type == PROJECT_TYPE

val ExtSubmission.computedTitle
    get(): String? = title ?: section.title

fun ExtSubmission.allFiles(): Sequence<ExtFile> = allFileListFiles().plus(allSectionsFiles).plus(pageTabFiles)

/**
 * Returns all file list files. Note that sequence is used instead regular iterable to avoid loading all submission
 * files before start processing.
 */
fun ExtSubmission.allFileListFiles(): Sequence<ExtFile> =
    allFileList
        .flatMap { it.files + it.pageTabFiles }
        .asSequence()
