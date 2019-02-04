package ac.uk.ebi.pmc.scheduler.pmc.importer

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec
import ac.uk.ebi.pmc.scheduler.common.properties.AppProperties
import ac.uk.ebi.scheduler.properties.ImportMode
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
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

        val properties = PmcImporterProperties(
            mode = ImportMode.GZ_FILE,
            path = file.absolutePath,
            temp = properties.temp,
            mongodbUri = properties.mongoUri,
            bioStudiesUrl = properties.bioStudiesUrl,
            bioStudiesUser = properties.bioStudiesUser,
            bioStudiesPassword = properties.bioStudiesPassword)

        val jobTry = clusterOperations.triggerJob(
            JobSpec(8, MemorySpec.SIXTEEN_GB, properties.asJavaCommand(appProperties.appsFolder), jobs))
        return jobTry.fold({ throw it }, { it })
    }

    fun importGzipFolder(files: List<File>) = files.chunked(PARALLELISM)
        .fold(emptyList<Job>()) { dependencyJobs, chunk -> chunk.map { importGzipFile(it, dependencyJobs) } }
}
