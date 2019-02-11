package ac.uk.ebi.pmc.process

import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking

class PmcSubmissionProcessor(private val pmcProcessor: PmcProcessor) {

    fun processSubmissions() {
        runBlocking { pmcProcessor.processSubmissions().joinAll() }
    }
}
