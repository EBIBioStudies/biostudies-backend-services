package ac.uk.ebi.biostd.persistence.doc.test.doc

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.StatusDraft.ACTIVE
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.StatusDraft.BEING_PROCESSED

internal const val USER_ID = "jhon.doe@ebi.ac.uk"
internal const val DRAFT_KEY = "key"
internal const val DRAFT_CONTENT = "content"

internal const val USER_ID1 = "jhon.doe1@ebi.ac.uk"
internal const val DRAFT_KEY1 = "key1"
internal const val DRAFT_CONTENT1 = "content1"

internal val testActiveDocDraft = DocSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT, ACTIVE)
internal val testBeingProcessedDocDraft = DocSubmissionDraft(USER_ID1, DRAFT_KEY1, DRAFT_CONTENT1, BEING_PROCESSED)
