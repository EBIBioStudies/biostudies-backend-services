package ac.uk.ebi.pmc.submit

import kotlinx.coroutines.runBlocking

class PmcBatchSubmitter(private val pmcSubmitter: PmcSubmitter) {
    fun submit() = runBlocking { pmcSubmitter.submit() }
}
