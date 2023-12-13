package uk.ac.ebi.biostd.client.cluster.exception

import uk.ac.ebi.biostd.client.cluster.model.Job

class FailedJobException(job: Job) : RuntimeException("The job ${job.id} execution has failed. Logs: ${job.logsPath}")
