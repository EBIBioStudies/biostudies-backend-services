package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.Mode
import ac.uk.ebi.biostd.common.properties.SubmissionTaskProperties
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec.Companion.SIXTEEN_GB

@Suppress("TooManyFunctions")
class RemoteExtSubmissionSubmitter(
    private val submissionTaskProperties: SubmissionTaskProperties,
    private val clusterClient: ClusterClient,
) : ExtSubmissionSubmitter {
    override suspend fun createRequest(rqt: ExtSubmitRequest): Pair<String, Int> {
        TODO("Not yet implemented")
    }

    override suspend fun indexRequest(accNo: String, version: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun loadRequest(accNo: String, version: Int) {
        executeRemotely(accNo, version, Mode.LOAD)
    }

    override suspend fun generatePageTabRequest(accNo: String, version: Int) {
        executeRemotely(accNo, version, Mode.GENERATE_PAGE_TAB)
    }

    override suspend fun cleanRequest(accNo: String, version: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun processRequest(accNo: String, version: Int) {
        executeRemotely(accNo, version, Mode.COPY)
    }

    override suspend fun checkReleased(accNo: String, version: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun saveRequest(accNo: String, version: Int): ExtSubmission {
        TODO("Not yet implemented")
    }

    override suspend fun finalizeRequest(accNo: String, version: Int): ExtSubmission {
        TODO("Not yet implemented")
    }

    override suspend fun release(accNo: String) {
        TODO("Not yet implemented")
    }

    override suspend fun handleRequest(accNo: String, version: Int): ExtSubmission {
        TODO("Not yet implemented")
    }

    private suspend fun executeRemotely(accNo: String, version: Int, mode: Mode) = withContext(Dispatchers.IO) {
        // TODO we should pass java home as an application property
        val command = buildList {
            add("/nfs/biostudies/.adm/java/zulu11.45.27-ca-jdk11.0.10-linux_x64/bin/java")
            add("-jar")
            add(submissionTaskProperties.jarLocation)
            add("--spring.config.location=${submissionTaskProperties.configFilePath}")
            add("--accNo=$accNo")
            add("--version=$version")
            add("--mode=${mode.name}")
        }
        val jobSpec = JobSpec(cores = 8, ram = SIXTEEN_GB, DataMoverQueue, command.joinToString(separator = " "))

        // TODO we might need to wait for the job indefinitely or make a rough estimate since it's hard to predict how long it'll take
        clusterClient.triggerJobSync(jobSpec, maxSecondsDuration = 1200)
    }
}
