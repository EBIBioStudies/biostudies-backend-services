package ac.uk.ebi.pmc.submit

import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking

class PmcBatchSubmitter(private val pmcSubmitter: PmcSubmitter) {
    fun submit() = runBlocking {
        pmcSubmitter.submit().joinAll()
    }
}
