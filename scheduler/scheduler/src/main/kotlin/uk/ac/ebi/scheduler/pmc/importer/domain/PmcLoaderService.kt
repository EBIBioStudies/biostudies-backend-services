package uk.ac.ebi.scheduler.pmc.importer.domain

import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import ac.uk.ebi.scheduler.properties.PmcMode
import ac.uk.ebi.scheduler.properties.PmcMode.LOAD
import ac.uk.ebi.scheduler.properties.PmcMode.PROCESS
import ac.uk.ebi.scheduler.properties.PmcMode.SUBMIT
import ac.uk.ebi.scheduler.properties.PmcMode.SUBMIT_SINGLE
import ebi.ac.uk.commons.http.slack.NotificationsSender
import ebi.ac.uk.commons.http.slack.Report
import mu.KotlinLogging
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.CoresSpec.EIGHT_CORES
import uk.ac.ebi.biostd.client.cluster.model.CoresSpec.FOUR_CORES
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec.Companion.EIGHT_GB
import uk.ac.ebi.scheduler.common.SYSTEM_NAME
import uk.ac.ebi.scheduler.common.properties.AppProperties
import uk.ac.ebi.scheduler.pmc.importer.api.PmcProcessorProp

private val logger = KotlinLogging.logger {}

internal const val LOADER_SUBSYSTEM = "PMC Loading Trigger"
internal const val PROCESSOR_SUBSYSTEM = "PMC Processor Trigger"
internal const val SUBMITTER_SUBSYSTEM = "PMC Submitter Trigger"

internal class PmcLoaderService private constructor(
    private val pmcLoaderService: PmcLoader,
    private val pmcNotificationsSender: NotificationsSender,
) {
    constructor(
        clusterClient: ClusterClient,
        properties: PmcProcessorProp,
        appProperties: AppProperties,
        notificationsSender: NotificationsSender,
    ) : this(PmcLoader(clusterClient, properties, appProperties), notificationsSender)

    suspend fun loadFile(
        folder: String?,
        file: String?,
        debugPort: Int? = null,
    ): Job {
        val job = pmcLoaderService.loadFile(folder, file, debugPort)
        val params =
            buildList {
                folder?.let { add("folder='$it'") }
                file?.let { add("file='$it'") }
                debugPort?.let { add("debugPort='$it'") }
            }.joinToString()

        pmcNotificationsSender.send(
            Report(
                SYSTEM_NAME,
                LOADER_SUBSYSTEM,
                "Triggered PMC loader params=[$params], cluster job: $job, logs will be available at ${job.logsPath}",
            ),
        )
        return job
    }

    suspend fun triggerProcessor(
        sourceFile: String?,
        debugPort: Int? = null,
    ): Job {
        val job = pmcLoaderService.triggerProcessor(sourceFile, debugPort)
        val params =
            buildList {
                sourceFile?.let { add("sourceFile='$it'") }
                debugPort?.let { add("debugPort='$it'") }
            }.joinToString()
        pmcNotificationsSender.send(
            Report(
                SYSTEM_NAME,
                PROCESSOR_SUBSYSTEM,
                "Triggered PMC processor params=[$params], cluster job: $job, logs will be available at ${job.logsPath}",
            ),
        )
        return job
    }

    suspend fun triggerSubmitter(
        sourceFile: String?,
        debugPort: Int? = null,
    ): Job {
        val job = pmcLoaderService.triggerSubmitter(sourceFile, debugPort)
        pmcNotificationsSender.send(
            Report(
                SYSTEM_NAME,
                SUBMITTER_SUBSYSTEM,
                "Triggered PMC submitter, cluster job: $job, logs will be available at ${job.logsPath}",
            ),
        )
        return job
    }

    suspend fun triggerSubmitSingle(
        debugPort: Int? = null,
        submissionId: String,
    ): Job {
        val job = pmcLoaderService.triggerSubmitSingle(submissionId, debugPort)
        pmcNotificationsSender.send(
            Report(
                SYSTEM_NAME,
                SUBMITTER_SUBSYSTEM,
                "Triggered PMC submitter['$submissionId'], cluster job: $job, logs will be available at ${job.logsPath}",
            ),
        )
        return job
    }
}

private class PmcLoader(
    private val clusterClient: ClusterClient,
    private val properties: PmcProcessorProp,
    private val appProperties: AppProperties,
) {
    suspend fun loadFile(
        folder: String?,
        file: String?,
        debugPort: Int?,
    ): Job {
        val loadFolder = folder ?: properties.loadFolder
        logger.info { "submitting job to load folder: '$folder'" }

        val properties = getConfigProperties(loadFolder = loadFolder, lodFile = file, importMode = LOAD)
        val jobTry =
            clusterClient.triggerJobAsync(
                JobSpec(
                    FOUR_CORES,
                    EIGHT_GB,
                    command = properties.asCmd(appProperties.appsFolder, debugPort),
                ),
            )
        return jobTry.fold({ it.apply { logger.info { "submitted job $it" } } }, { throw it })
    }

    suspend fun triggerProcessor(
        sourceFile: String?,
        debugPort: Int?,
    ): Job {
        logger.info { "submitting job to process submissions, source file ${sourceFile ?: "any"}" }
        val properties = getConfigProperties(importMode = PROCESS, sourceFile = sourceFile)
        val jobTry =
            clusterClient.triggerJobAsync(
                JobSpec(
                    FOUR_CORES,
                    EIGHT_GB,
                    command = properties.asCmd(appProperties.appsFolder, debugPort),
                ),
            )
        return jobTry.fold({ it.apply { logger.info { "submitted job $it" } } }, { throw it })
    }

    suspend fun triggerSubmitter(
        sourceFile: String?,
        debugPort: Int?,
    ): Job {
        logger.info { "submitting job to submit submissions, source file ${sourceFile ?: "any"}" }
        val properties = getConfigProperties(importMode = SUBMIT, sourceFile = sourceFile)
        val jobTry =
            clusterClient.triggerJobAsync(
                JobSpec(
                    EIGHT_CORES,
                    MemorySpec.TWENTYFOUR_GB,
                    command = properties.asCmd(appProperties.appsFolder, debugPort),
                ),
            )
        return jobTry.fold({ it.apply { logger.info { "submitted job $it" } } }, { throw it })
    }

    suspend fun triggerSubmitSingle(
        submissionId: String,
        debugPort: Int?,
    ): Job {
        logger.info { "submitting job to submit submissions" }
        val properties = getConfigProperties(importMode = SUBMIT_SINGLE, submissionId = submissionId)
        val jobTry =
            clusterClient.triggerJobAsync(
                JobSpec(
                    EIGHT_CORES,
                    MemorySpec.TWENTYFOUR_GB,
                    command = properties.asCmd(appProperties.appsFolder, debugPort),
                ),
            )
        return jobTry.fold({ it.apply { logger.info { "submitted job $it" } } }, { throw it })
    }

    private fun getConfigProperties(
        loadFolder: String? = null,
        lodFile: String? = null,
        submissionId: String? = null,
        sourceFile: String? = null,
        importMode: PmcMode,
    ) = PmcImporterProperties.create(
        mode = importMode,
        loadFolder = loadFolder,
        loadFile = lodFile,
        sourceFile = sourceFile,
        temp = properties.temp,
        mongodbUri = properties.mongoUri,
        mongodbDatabase = properties.mongoDatabase,
        notificationsUrl = appProperties.slack.pmcNotificationsUrl,
        pmcBaseUrl = "http://www.ft-loading.europepmc.org",
        bioStudiesUser = properties.bioStudiesUser,
        bioStudiesPassword = properties.bioStudiesPassword,
        bioStudiesUrl = properties.bioStudiesUrl,
        submissionId = submissionId,
    )
}
