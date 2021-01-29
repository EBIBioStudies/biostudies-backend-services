package ac.uk.ebi.biostd.persistence.doc.test.doc

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft

internal const val USER_ID = 1234L
internal const val DRAFT_KEY = "key"
internal const val DRAFT_CONTENT = "content"

internal val testDocDraft = DocSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT)
