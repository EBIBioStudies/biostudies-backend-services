package uk.ac.ebi.scheduler.pmc.importer.domain

import ac.uk.ebi.cluster.client.lsf.ClusterOperations
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import ac.uk.ebi.cluster.client.model.MemorySpec
import ac.uk.ebi.cluster.client.model.logsPath
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ac.uk.ebi.scheduler.properties.PmcMode
import ac.uk.ebi.scheduler.properties.PmcMode.LOAD
import ac.uk.ebi.scheduler.properties.PmcMode.PROCESS
import ac.uk.ebi.scheduler.properties.PmcMode.SUBMIT
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import mu.KotlinLogging
import uk.ac.ebi.scheduler.common.SYSTEM_NAME
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.pmc.importer.api.PmcProcessorProp

private val logger = KotlinLogging.logger {}

internal const val LOADER_SUBSYSTEM = "PMC Loading Trigger"
internal const val PROCESSOR_SUBSYSTEM = "PMC Processor Trigger"
internal const val SUBMITTER_SUBSYSTEM = "PMC Submitter Trigger"

internal class PmcLoaderService private constructor(
    private val pmcLoaderService: PmcLoader,
    private val notificationsSender: NotificationsSender
) {
    constructor(
        clusterOperations: ClusterOperations,
        properties: PmcProcessorProp,
        appProperties: AppProperties,
        notificationsSender: NotificationsSender
    ) : this(PmcLoader(clusterOperations, properties, appProperties), notificationsSender)

    fun loadFile(file: String?): Job {
        val job = pmcLoaderService.loadFile(file)
        notificationsSender.send(
            Report(
                SYSTEM_NAME,
                LOADER_SUBSYSTEM,
                "Triggered PMC loader $file, cluster job: $job, logs will be available at ${job.logsPath}"
            )
        )
        return job
    }

    fun triggerProcessor(): Job {
        val job = pmcLoaderService.triggerProcessor()
        notificationsSender.send(
            Report(
                SYSTEM_NAME,
                PROCESSOR_SUBSYSTEM,
                "Triggered PMC processor, cluster job: $job, logs will be available at ${job.logsPath}"
            )
        )
        return job
    }

    fun triggerSubmitter(): Job {
        val job = pmcLoaderService.triggerSubmitter()
        notificationsSender.send(
            Report(
                SYSTEM_NAME,
                SUBMITTER_SUBSYSTEM,
                "Triggered PMC submitter, cluster job: $job, logs will be available at ${job.logsPath}"
            )
        )
        return job
    }
}

private const val FOUR_CORES = 4
private const val EIGHT_CORES = 8

private class PmcLoader(
    private val clusterOperations: ClusterOperations,
    private val properties: PmcProcessorProp,
    private val appProperties: AppProperties
) {

    fun loadFile(loadFolder: String?): Job {
        val folder = loadFolder ?: properties.loadFolder
        logger.info { "submitting job to load folder: '$folder'" }

        val properties = getConfigProperties(folder, LOAD)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                FOUR_CORES,
                MemorySpec.EIGHT_GB,
                properties.asJavaCommand(appProperties.appsFolder)
            )
        )
        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    fun triggerProcessor(): Job {
        logger.info { "submitting job to process submissions" }
        val properties = getConfigProperties(importMode = PROCESS)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                FOUR_CORES,
                MemorySpec.EIGHT_GB,
                properties.asJavaCommand(appProperties.appsFolder)
            )
        )
        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    fun triggerSubmitter(): Job {
        logger.info { "submitting job to submit submissions" }
        val properties = getConfigProperties(importMode = SUBMIT)
        val jobTry = clusterOperations.triggerJob(
            JobSpec(
                EIGHT_CORES,
                MemorySpec.TWENTYFOUR_GB,
                properties.asJavaCommand(appProperties.appsFolder)
            )
        )
        return jobTry.fold({ throw it }, { it.apply { logger.info { "submitted job $it" } } })
    }

    private fun getConfigProperties(loadFolder: String? = null, importMode: PmcMode) =
        PmcImporterProperties.create(
            mode = importMode,
            loadFolder = loadFolder,
            temp = properties.temp,
            mongodbUri = properties.mongoUri,
            mongodbDatabase = properties.mongoDatabase,
            notificationsUrl = appProperties.notificationsUrl,
            pmcBaseUrl = "http://www.ft-loading.europepmc.org",
            bioStudiesUser = properties.bioStudiesUser,
            bioStudiesPassword = properties.bioStudiesPassword,
            bioStudiesUrl = properties.bioStudiesUrl
        )
}
