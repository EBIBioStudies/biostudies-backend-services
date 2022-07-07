package uk.ac.ebi.scheduler.exporter.service

import kotlinx.coroutines.runBlocking

class ExporterService(
    private val pmcExporterService: PmcExporterService,
    private val publicOnlyExporterService: PublicOnlyExporterService
) {
    fun exportPmc() {
        runBlocking { pmcExporterService.exportPmcLinks() }
    }

    fun exportPublicOnly() {
        publicOnlyExporterService.exportPublicSubmissions()
    }
}
