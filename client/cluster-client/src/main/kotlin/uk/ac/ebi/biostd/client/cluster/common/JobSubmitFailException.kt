package uk.ac.ebi.biostd.client.cluster.common

class JobSubmitFailException(
    exitCode: Int,
    override val message: String,
) : RuntimeException("Job Triggering failure exit code: '$exitCode'. $message")
