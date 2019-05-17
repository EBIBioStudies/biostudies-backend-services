package ac.uk.ebi.biostd.itest.common

import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.junit.jupiter.api.BeforeAll

internal open class BaseIntegrationTest(private val tempFolder: TemporaryFolder) {
    private lateinit var filesDirPath: String
    protected lateinit var basePath: String

    @BeforeAll
    fun init() {
        tempFolder.createDirectory("dropbox")

        basePath = tempFolder.root.absolutePath
        filesDirPath = "$basePath/dropbox"

        System.setProperty("app.basepath", basePath)
        System.setProperty("app.filesDirPath", filesDirPath)
    }
}
