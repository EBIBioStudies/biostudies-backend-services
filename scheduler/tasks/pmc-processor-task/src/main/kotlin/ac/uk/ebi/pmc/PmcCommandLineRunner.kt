package ac.uk.ebi.pmc

import org.springframework.boot.CommandLineRunner

class PmcCommandLineRunner(
    private val pmcTaskExecutor: PmcTaskExecutor,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        pmcTaskExecutor.run()
    }
}
