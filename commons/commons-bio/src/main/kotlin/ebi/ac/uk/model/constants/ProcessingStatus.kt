package ebi.ac.uk.model.constants

import ebi.ac.uk.errors.UnsupportedStatusException

private const val PROCESSED_STATUS = "PROCESSED"
private const val PROCESSING_STATUS = "PROCESSING"

sealed class ProcessingStatus(val value: String) {
    companion object {
        fun valueOf(value: String): ProcessingStatus = when (value) {
            PROCESSED_STATUS -> Processed
            PROCESSING_STATUS -> Processing
            else -> throw UnsupportedStatusException(value)
        }
    }
}

object Processed : ProcessingStatus(PROCESSED_STATUS)
object Processing : ProcessingStatus(PROCESSING_STATUS)
