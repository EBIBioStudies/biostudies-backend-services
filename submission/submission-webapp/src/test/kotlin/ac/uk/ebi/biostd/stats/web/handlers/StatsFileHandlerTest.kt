package ac.uk.ebi.biostd.stats.web.handlers

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.submission.stats.InvalidStatException
import ac.uk.ebi.biostd.submission.stats.StatsFileHandler
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class StatsFileHandlerTest(
    private val tempFolder: TemporaryFolder,
) {
    private val testInstance = StatsFileHandler()

    @Test
    fun `read stats`() {
        val fileContent = tsv {
            line("S-TEST123", 10)
            line("S-TEST124", 20)
        }
        val statsFile = tempFolder.createFile("stats.tsv", fileContent.toString())
        val stats = testInstance.readStats(statsFile, VIEWS)

        assertThat(stats).hasSize(2)
        assertStat(stats.first(), "S-TEST123", 10L)
        assertStat(stats.second(), "S-TEST124", 20L)
    }

    @Test
    fun `invalid stat`() {
        val fileContent = tsv {
            line("S-TEST123", 10)
            line("S-TEST124")
        }
        val statsFile = tempFolder.createFile("invalid-stats.tsv", fileContent.toString())

        val exception = assertThrows<InvalidStatException> { testInstance.readStats(statsFile, VIEWS) }
        assertThat(exception.message).isEqualTo("The stats should have accNo and value")
    }

    private fun assertStat(stat: SubmissionStat, accNo: String, value: Long) {
        assertThat(stat.accNo).isEqualTo(accNo)
        assertThat(stat.value).isEqualTo(value)
        assertThat(stat.type).isEqualTo(VIEWS)
    }
}
