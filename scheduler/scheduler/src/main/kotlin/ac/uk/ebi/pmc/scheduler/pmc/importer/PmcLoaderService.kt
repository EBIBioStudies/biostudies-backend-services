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
private const val TASK_CORES = 8

class PmcLoaderService(
    private val clusterOperations: ClusterOperations,
    private val properties: PmcProcessorProp,
    private val appProperties: AppProperties
) {

    fun loadFile(file: File): Job {
        logger.info { "submitting job to load file ${file.absolutePath}" }

        val properties = getConfigProperties(file, PmcMode.LOAD)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                TASK_CORES,
                MemorySpec.SIXTEEN_GB,
                properties.asJavaCommand(appProperties.appsFolder)))
        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    fun process() {
        logger.info { "submitting job to process submissions" }
        val properties = getConfigProperties(importMode = PmcMode.LOAD)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                TASK_CORES,
                MemorySpec.SIXTEEN_GB,
                properties.asJavaCommand(appProperties.appsFolder)))
        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    private fun getConfigProperties(file: File? = null, importMode: PmcMode) = PmcImporterProperties(
        mode = importMode,
        path = file?.absolutePath,
        temp = properties.temp,
        mongodbUri = properties.mongoUri,
        bioStudiesUrl = properties.bioStudiesUrl,
        bioStudiesUser = properties.bioStudiesUser,
        bioStudiesPassword = properties.bioStudiesPassword)
}
