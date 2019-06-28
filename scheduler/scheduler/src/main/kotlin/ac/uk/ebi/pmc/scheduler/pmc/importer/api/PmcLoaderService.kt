package ac.uk.ebi.pmc.scheduler.pmc.importer.api

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec
import ac.uk.ebi.pmc.scheduler.common.properties.AppProperties
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ac.uk.ebi.scheduler.properties.PmcMode
import ac.uk.ebi.scheduler.properties.PmcMode.LOAD
import ac.uk.ebi.scheduler.properties.PmcMode.PROCESS
import ac.uk.ebi.scheduler.properties.PmcMode.SUBMIT
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
private const val FOUR_CORES = 4
private const val EIGHT_CORES = 8

internal class PmcLoaderService(
    private val clusterOperations: ClusterOperations,
    private val properties: PmcProcessorProp,
    private val appProperties: AppProperties
) {

    fun loadFile(filePath: String): Job {
        logger.info { "submitting job to load folder: '$filePath'" }

        val properties = getConfigProperties(filePath, LOAD, appProperties.notificationsUrl)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                FOUR_CORES,
                MemorySpec.EIGHT_GB,
                properties.asJavaCommand(appProperties.appsFolder)))
        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    fun triggerProcessor(): Job {
        logger.info { "submitting job to process submissions" }
        val properties = getConfigProperties(importMode = PROCESS, notificationsUrl = appProperties.notificationsUrl)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                FOUR_CORES,
                MemorySpec.EIGHT_GB,
                properties.asJavaCommand(appProperties.appsFolder)))
        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    fun triggerSubmitter(): Job {
        logger.info { "submitting job to submit submissions" }
        val properties = getConfigProperties(importMode = SUBMIT, notificationsUrl = appProperties.notificationsUrl)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                EIGHT_CORES,
                MemorySpec.TWENTYFOUR_GB,
                properties.asJavaCommand(appProperties.appsFolder)))
        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    private fun getConfigProperties(filePath: String? = null, importMode: PmcMode, notificationsUrl: String) =
        PmcImporterProperties.create(
            mode = importMode,
            path = filePath,
            temp = properties.temp,
            mongodbUri = properties.mongoUri,
            notificationsUrl = notificationsUrl,
            bioStudiesUser = properties.bioStudiesUser,
            bioStudiesPassword = properties.bioStudiesPassword)
}
