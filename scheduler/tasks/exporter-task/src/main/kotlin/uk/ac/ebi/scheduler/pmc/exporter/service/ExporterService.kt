package uk.ac.ebi.scheduler.pmc.exporter.service

import kotlinx.coroutines.runBlocking

class ExporterService(
    private val pmcExporterService: PmcExporterService,
    private val publicOnlyExporterService: PublicOnlyExporterService,
) {
    fun updatePmcView() {
        runBlocking { pmcExporterService.updateView() }
    }

    fun exportPmc() {
        runBlocking { pmcExporterService.exportPmcLinks() }
    }

    fun exportPublicOnly() {
        publicOnlyExporterService.exportPublicSubmissions()
    }
}
