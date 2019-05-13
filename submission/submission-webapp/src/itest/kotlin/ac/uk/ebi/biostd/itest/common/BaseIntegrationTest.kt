package ac.uk.ebi.biostd.itest.common

import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.junit.jupiter.api.BeforeAll

internal open class BaseIntegrationTest(private val tempFolder: TemporaryFolder) {

    protected lateinit var basePath: String

    @BeforeAll
    fun init() {
        basePath = tempFolder.root.absolutePath
        System.setProperty("app.basepath", basePath)
    }
}
