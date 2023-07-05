package uk.ac.ebi.io.builder

import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allInnerSubmissionFiles
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.paths.FILES_PATH
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.io.sources.DbFilesSource
import uk.ac.ebi.io.sources.FilesListSource
import uk.ac.ebi.io.sources.FireFilesSource
import uk.ac.ebi.io.sources.GroupPathSource
import uk.ac.ebi.io.sources.PathSource
import uk.ac.ebi.io.sources.SubmissionFilesSource
import uk.ac.ebi.io.sources.UserPathSource
import java.io.File
import java.nio.file.Path

class FilesSourceListBuilder(
    internal val submissionPath: Path,
    internal val fireClient: FireClient,
    internal val filesRepository: SubmissionFilesPersistenceService,
    internal val sources: MutableList<FilesSource> = mutableListOf(),
) {
    fun build(): FileSourcesList = FileSourcesList(sources.toList())
}

fun FilesSourceListBuilder.buildFilesSourceList(builderAction: FilesSourceListBuilder.() -> Unit): FileSourcesList {
    this.sources.clear()
    return this.apply { builderAction() }.build()
}

fun FilesSourceListBuilder.addDbFilesSource() {
    sources.add(DbFilesSource)
}

fun FilesSourceListBuilder.addFilesListSource(files: List<File>) {
    sources.add(FilesListSource(files))
}

fun FilesSourceListBuilder.addFireFilesSource() {
    sources.add(FireFilesSource(fireClient))
}

fun FilesSourceListBuilder.addUserSource(description: String, sourcePath: Path) {
    sources.add(UserPathSource(description, sourcePath))
}

fun FilesSourceListBuilder.addGroupSource(groupName: String, sourcePath: Path) {
    sources.add(GroupPathSource(groupName, sourcePath))
}

fun FilesSourceListBuilder.addSubmissionSource(submission: ExtSubmission) {
    val nfsSubPath = submissionPath.resolve("${submission.relPath}/$FILES_PATH")
    val nfsFiles = PathSource("Previous version files", nfsSubPath)
    val previousVersionFiles = submission
        .allInnerSubmissionFiles
        .groupBy { it.filePath }
        .mapValues { it.value.first() }

    sources.add(SubmissionFilesSource(submission, nfsFiles, fireClient, previousVersionFiles, filesRepository))
}
