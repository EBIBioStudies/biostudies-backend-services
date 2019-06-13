package ac.uk.ebi.biostd.itest.common

import io.github.glytching.junit.extension.folder.TemporaryFolder
import org.junit.jupiter.api.BeforeAll

internal open class BaseIntegrationTest(private val tempFolder: TemporaryFolder) {
    protected lateinit var basePath: String

    @BeforeAll
    fun init() {
        val dropbox = tempFolder.createDirectory("dropbox")
        val temp = tempFolder.createDirectory("tmp")
        basePath = tempFolder.root.absolutePath

        System.setProperty("app.basepath", tempFolder.root.absolutePath)
        System.setProperty("app.tempDirPath", temp.absolutePath)
        System.setProperty("app.security.filesDirPath", dropbox.absolutePath)
    }
}
