package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionRequestFilesRepository

class SubmissionRequestFilesDocDataRepository(
    private val submissionRequestFilesRepository: SubmissionRequestFilesRepository,
) : SubmissionRequestFilesRepository by submissionRequestFilesRepository
