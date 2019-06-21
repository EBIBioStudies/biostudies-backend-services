package ebi.ac.uk.submission.processing.security

import java.util.UUID

internal fun newSecurityKey() = UUID.randomUUID().toString()
