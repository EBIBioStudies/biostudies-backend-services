package ac.uk.ebi.pmc.scheduler.pmc.importer

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec
import ac.uk.ebi.pmc.scheduler.common.properties.AppProperties
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ac.uk.ebi.scheduler.properties.PmcMode
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}
private const val PARALLELISM = 5

class PmcImporterService(
    private val clusterOperations: ClusterOperations,
    private val properties: SchedulerPmcImporterProp,
    private val appProperties: AppProperties
) {

    fun importGzipFile(file: File, jobs: List<Job> = emptyList()): Job {
        logger.info { "submitting job to process file ${file.absolutePath}" }

        val properties = getConfigProperties(file, PmcMode.GZ_FILE)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                8,
                MemorySpec.SIXTEEN_GB,
                properties.asJavaCommand(appProperties.appsFolder),
                jobs))
        return jobTry.fold({ throw it }, { it })
    }

    fun importGzipFolder(files: List<File>) = files.chunked(PARALLELISM)
        .fold(emptyList<Job>()) { dependencyJobs, chunk -> chunk.map { importGzipFile(it, dependencyJobs) } }

    fun submitFile(file: File): Job {
        logger.info { "submitting job to submit file ${file.absolutePath}" }

        val properties = getConfigProperties(file, PmcMode.SUBMIT)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                8,
                MemorySpec.SIXTEEN_GB,
                properties.asJavaCommand(appProperties.appsFolder)))
        return jobTry.fold({ throw it }, { it })
    }

    private fun getConfigProperties(file: File, importMode: PmcMode) = PmcImporterProperties(
        mode = importMode,
        path = file.absolutePath,
        temp = this.properties.temp,
        mongodbUri = this.properties.mongoUri,
        bioStudiesUrl = this.properties.bioStudiesUrl,
        bioStudiesUser = this.properties.bioStudiesUser,
        bioStudiesPassword = this.properties.bioStudiesPassword)
}
